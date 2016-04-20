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
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import eu.trentorise.game.challenges.rest.Content;
import eu.trentorise.game.challenges.rest.GamificationEngineRestFacade;
import eu.trentorise.game.challenges.rest.InsertedRuleDto;

public class StressTest {

	private GamificationEngineRestFacade facade;
	private GamificationEngineRestFacade insertFacade;

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

}
