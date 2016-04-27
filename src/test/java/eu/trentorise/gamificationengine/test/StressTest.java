package eu.trentorise.gamificationengine.test;

import static eu.trentorise.challenge.PropertiesUtil.CONTEXT;
import static eu.trentorise.challenge.PropertiesUtil.GAMEID;
import static eu.trentorise.challenge.PropertiesUtil.HOST;
import static eu.trentorise.challenge.PropertiesUtil.INSERT_CONTEXT;
import static eu.trentorise.challenge.PropertiesUtil.PASSWORD;
import static eu.trentorise.challenge.PropertiesUtil.USERNAME;
import static eu.trentorise.challenge.PropertiesUtil.get;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import eu.trentorise.game.challenges.rest.Content;
import eu.trentorise.game.challenges.rest.ExecutionDataDTO;
import eu.trentorise.game.challenges.rest.GamificationEngineRestFacade;
import eu.trentorise.game.challenges.rest.InsertedRuleDto;

public class StressTest {

	private GamificationEngineRestFacade facade;
	private GamificationEngineRestFacade insertFacade;
	private final static String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

	@Before
	public void setup() {
		facade = new GamificationEngineRestFacade(get(HOST) + get(CONTEXT),
				get(USERNAME), get(PASSWORD));
		insertFacade = new GamificationEngineRestFacade(get(HOST)
				+ get(INSERT_CONTEXT), get(USERNAME), get(PASSWORD));
	}

	@Test
	public void gameReadGameStateTest() {
		List<Content> result = facade.readGameState(get(GAMEID));
		assertTrue(!result.isEmpty());
	}

	@Test
	public void stressTestRead() throws FileNotFoundException, IOException {
		StringBuffer toWrite = new StringBuffer();

		toWrite.append("N;TIME\n");
		int n = 1000;
		long start = 0;
		long end = 0;
		for (int i = 0; i < n; i++) {
			toWrite.append(i + ";");
			start = System.currentTimeMillis();
			List<Content> result = facade.readGameState(get(GAMEID));
			end = System.currentTimeMillis();
			toWrite.append(Math.abs(end - start) + "\n");
		}

		IOUtils.write(toWrite.toString(), new FileOutputStream(
				"stressTestRead.csv"));
	}

	@Test
	public void stressTestInsertAndRead() throws FileNotFoundException,
			IOException {
		StringBuffer toWrite = new StringBuffer();

		toWrite.append("N;TIME\n");
		int n = 1000;
		long start = 0;
		long end = 0;

		// insert 1000 rules

		// define rule
		List<String> inserted = new ArrayList<String>();
		for (int i = 0; i < n; i++) {
			InsertedRuleDto rule = new InsertedRuleDto();
			rule.setContent("/* */");
			rule.setName("sampleRule" + i);
			// insert rule
			InsertedRuleDto result = insertFacade.insertGameRule(get(GAMEID),
					rule);
			inserted.add(result.getId());
		}

		// write stress test report
		for (int i = 0; i < n; i++) {
			toWrite.append(i + ";");
			start = System.currentTimeMillis();
			List<Content> result = facade.readGameState(get(GAMEID));
			end = System.currentTimeMillis();
			toWrite.append(Math.abs(end - start) + "\n");
		}

		IOUtils.write(toWrite.toString(), new FileOutputStream(
				"stressTestInsertAndRead.csv"));

		// delete all inserted rules
		for (String ruleId : inserted) {
			insertFacade.deleteGameRule(get(GAMEID), ruleId);
		}
	}

	@Test
	public void massUpdateCustomData() {
		// read game state
		List<Content> result = facade.readGameState(get(GAMEID));
		assertTrue(!result.isEmpty());

		// create fake custom data
		int n = 200;
		Map<String, Object> customData = new HashMap<String, Object>();
		for (int i = 0; i < n; i++) {
			customData.put("customData_" + i, LOREM_IPSUM);
		}
		// for every user update customdata
		for (Content user : result) {
			insertFacade.updateChallengeCustomData(get(GAMEID),
					user.getPlayerId(), customData);
		}
	}

	@Test
	public void massCreate100rules() throws FileNotFoundException, IOException {
		// define rule valid for a specific rule ( in this case 16 )
		InsertedRuleDto rule = new InsertedRuleDto();
		rule.setContent(IOUtils.toString(StressTest.class
				.getResourceAsStream("StressTestOnlyFor16.drl")));

		// insert 10 times this rule
		for (int i = 0; i < 10; i++) {
			UUID uuid = UUID.randomUUID();
			rule.setContent(changeRuleName(rule.getContent(), uuid.toString()));
			rule.setName("sampleRule_" + uuid.toString());
			InsertedRuleDto result = insertFacade.insertGameRule(get(GAMEID),
					rule);
		}

		// insert other 90 rules not related to user 16
		rule = new InsertedRuleDto();
		rule.setContent(IOUtils.toString(StressTest.class
				.getResourceAsStream("StressTestNotFor16.drl")));
		// insert 10 times this rule
		for (int i = 0; i < 90; i++) {
			UUID uuid = UUID.randomUUID();
			rule.setContent(changeRuleName(rule.getContent(), uuid.toString()));
			rule.setName("sampleRule_" + UUID.randomUUID());
			InsertedRuleDto result = insertFacade.insertGameRule(get(GAMEID),
					rule);
		}
	}

	private String changeRuleName(String content, String uuid) {
		if (content.contains("PlayerCustomData")) {
			return StringUtils.replace(content, "PlayerCustomData",
					"PlayerCustomData" + uuid, 1);
		}
		return content;
	}

	@Test
	public void executeStressRuleForUser() throws FileNotFoundException,
			IOException {
		StringBuffer toWrite = new StringBuffer();
		toWrite.append("TIME\n");
		long start = 0;
		long end = 0;
		ExecutionDataDTO input = new ExecutionDataDTO();
		input.setActionId("stress_test");
		input.setGameId(get(GAMEID));
		input.setPlayerId("16");
		for (int i = 0; i < 100; i++) {
			start = System.currentTimeMillis();
			facade.executeAction(input);
			end = System.currentTimeMillis();
			toWrite.append(Math.abs(end - start) + "\n");
		}
		IOUtils.write(toWrite.toString(), new FileOutputStream(
				"stressRuleExecute.csv"));
	}

	@Test
	public void stressExecuteTest() throws FileNotFoundException, IOException {
		StringBuffer toWrite = new StringBuffer();
		toWrite.append("TIME\n");
		// execute 1000 times stress_test rules for every user into the game
		long start = 0;
		long end = 0;
		int n = 1000;
		// read game state
		List<Content> result = facade.readGameState(get(GAMEID));
		assertTrue(!result.isEmpty());

		for (int i = 0; i < n; i++) {
			// for every user call test action
			for (Content user : result) {
				ExecutionDataDTO input = new ExecutionDataDTO();
				input.setActionId("stress_test");
				input.setGameId(get(GAMEID));
				input.setPlayerId(user.getPlayerId());
				start = System.currentTimeMillis();
				facade.executeAction(input);
				end = System.currentTimeMillis();
				toWrite.append(Math.abs(end - start) + "\n");
			}
		}
		IOUtils.write(toWrite.toString(), new FileOutputStream(
				"stressExecute.csv"));
	}
}
