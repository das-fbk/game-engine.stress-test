# Gamification engine stress test

In this project there are some test to stress on loading [Gamification Engine](https://github.com/smartcommunitylab/smartcampus.gamification) from [Smart Community Lab](https://github.com/smartcommunitylab) and from [DAS](https://github.com/das-fbk)
 

## Description

Gamification engine stress test create a series of "limit" situations for [Gamification Engine](https://github.com/smartcommunitylab/smartcampus.gamification), including:

1. read game status for 100 times 
2. update for every user a lot of "fake" custom data (Lorem ipsum..)
3. create a set of rules automatically

As output for some junits test are created a set of csv files for time performance measure.


## Prerequisites 

* Java 1.7 or higher
* Maven 3.2 or higher
* Gamification engine, [setup guide here](https://github.com/smartcommunitylab/smartcampus.gamification/wiki/Setup)

## How to build

1. Clone repository with git
2. Clone [Gamification Engine Challenge Gen](https://github.com/das-fbk/game-engine.challenge-gen) project
3. Compile with maven using mvn install

## How to use

1. create a new game
2. register a set of players using [action save_itinerary](https://github.com/smartcommunitylab/smartcampus.gamification/wiki/Tutorial) or from backup 
3. create a new action "stress_test" from Gamification Engine administration console
4. Run a junit test
5. Analyze results in csv file

## License

Project is licensed under the Apache License Version 2.0


