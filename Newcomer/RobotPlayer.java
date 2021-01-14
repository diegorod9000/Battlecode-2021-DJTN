package Newcomer;

import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    static final RobotType[] spawnableRobot = { RobotType.POLITICIAN, RobotType.SLANDERER, RobotType.MUCKRAKER, };

    static final Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
            Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };

    static int turnCount;
    static MapLocation origin = null;

    /**
     * run() is the method that is called when a robot is instantiated in the
     * Battlecode world. If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this
        // robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each
                // RobotType.
                // You may rewrite this into your own control structure if you wish.
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER:
                        runEnlightenmentCenter();
                        break;
                    case POLITICIAN:
                        runPolitician();
                        break;
                    case SLANDERER:
                        runSlanderer();
                        break;
                    case MUCKRAKER:
                        runMuckraker();
                        break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform
                // this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }






    static Direction findDirection(int num) {
        int ModEight = (robotCount + num) % 8;
        Direction direc = Direction.NORTH;
        switch (ModEight) {
            case 0:      direc = Direction.NORTH;      break;
            case 1:      direc = Direction.SOUTH;      break;
            case 2:      direc = Direction.EAST;       break;
            case 3:      direc = Direction.WEST;       break;
            case 4:      direc = Direction.NORTHEAST;  break;
            case 5:      direc = Direction.SOUTHWEST;  break;
            case 6:      direc = Direction.NORTHWEST;  break;
            case 7:      direc = Direction.SOUTHEAST;  break;
        }

        if (rc.canBuildRobot(RobotType.POLITICIAN, direc, 1)) {
            return direc;
        } else {
            findDirection(++num);
        }
        return direc;
    }

    static void buildAlternating(int influence) throws GameActionException {
        //builds robots on a 4-round cycle, slanderes are built 2x as much as politicians an muckrakers
        //direction is automatically north but this can be changed, influence as well

        int roundModFour = robotCount % 4;
        switch (roundModFour) {
            case 0:     buildPolitician(influence);     break;
            case 1:     buildMuckraker(influence);     break;
            case 2:
            case 3:     buildSlanderer(influence);     break;

        }
    }

    static void buildPolitician(int influence) throws GameActionException {
        if (rc.canBuildRobot(RobotType.POLITICIAN, findDirection(0), influence)) {
            rc.buildRobot(RobotType.POLITICIAN, findDirection(0), influence);
            robotCount++;
            System.out.println("POLITICIAN built on round " + turnCount);
        }
    }

    static void buildSlanderer(int influence) throws GameActionException {
        if (rc.canBuildRobot(RobotType.SLANDERER, findDirection(0), influence)) {
            rc.buildRobot(RobotType.SLANDERER, findDirection(0), influence);
            robotCount++;
            System.out.println("SLANDERER built on round " + turnCount);
        }
    }

    static void buildMuckraker(int influence) throws GameActionException {
        if (rc.canBuildRobot(RobotType.MUCKRAKER, findDirection(0), influence)) {
            rc.buildRobot(RobotType.MUCKRAKER, findDirection(0), influence);
            robotCount++;
            System.out.println("MUCKRAKER built on round " + turnCount);
        }
    }

    static void bidPercent(double percent) throws GameActionException {
        //bid a certain percent of the center's influence

        int biddingInfluence = (int)(Math.round(rc.getInfluence() * percent));

        if (rc.canBid(Math.round(biddingInfluence))) {
            rc.bid(Math.round(biddingInfluence));
        }
    }

    static double calcQuadBidPercent() {
        //function that determines percent of influence used to bid, based on round number
        //quadratic, starts low and ends higher, starts at 20% and ends at 50%

        return (1.0/450000 * Math.pow(turnCount, 2) - 1.0/300 * turnCount + 20) / 100.0;
    }

    static double calcLinearBuildPercent() {
        //function that determines percent of influence used to build robots, based on round number
        //quadratic, starts high and ends low, starts at 50% and ends at 30%

        return (-1.0/150 * turnCount + 50) / 100.0;
    }


    static int robotCount;

    static void runEnlightenmentCenter() throws GameActionException {
        //building
        if (rc.senseNearbyRobots(40, rc.getTeam().opponent()).length > 4) {
            //builds politicians if there are lots of opponents nearby.
            buildPolitician((int)(Math.round(rc.getInfluence() * calcLinearBuildPercent())));
        } else {
            buildAlternating((int)(Math.round(rc.getInfluence() * calcLinearBuildPercent())));
        }

        //bidding
        if (turnCount % 3 == 0) {
            if (rc.canBid(1)) {
                rc.bid(1);
            }
        } else {
            bidPercent(calcQuadBidPercent());
        }
    }



    //Politician AI
    static void runPolitician() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        MapLocation[] enemies = rc.detectNearbyRobots();
        for (int i = 0; i < enemies.length;i++){
            continue;
        }
        boolean winning = true;

        if (rc.isReady()){
        }
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        RobotInfo[] nuetralECs = rc.senseNearbyRobots(actionRadius);
        boolean nearNeutralEC = false;
        for (int i = 0; i < nuetralECs.length; i++) {
            //System.out.println("Near " + nuetralECs[i].getType().toString());
            if (nuetralECs[i].getType().equals(RobotType.ENLIGHTENMENT_CENTER) && rc.canEmpower(actionRadius)) {
                System.out.println("Near Enlightenment Center with team" + nuetralECs[i].getTeam().toString() + " with conviction " + nuetralECs[i].getConviction());
                if (nuetralECs[i].getTeam().equals(rc.getTeam().opponent())){
                    System.out.println("yes");
                    rc.empower(actionRadius);
                    System.out.println("Attacking Enlightenment Center");
                    return;
                }
            }
        }
//        System.out.println("" + rc.getTeamVotes() + " " + rc.getRoundNum());
        double score = rc.getTeamVotes() / rc.getRoundNum();
        if (score >= .3) {
            if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
                System.out.println("empowering...");
                rc.empower(actionRadius);
                System.out.println("empowered");
                return;
            }

        } else {
            winning = false;
            //System.out.println("We are losing badly so I will not attack");
        }

        if (winning || attackable.length == 0){
            if (tryMove(randomDirection()))
                System.out.println("I moved!");
        }
        else{
            Direction[] available = new Direction[8];
            for (int i = 0; i < attackable.length && i < 8;i++){
                if (attackable[i].getTeam().equals(rc.getTeam().opponent())) {
                    available[i] = attackable[i].getLocation().directionTo(rc.getLocation());
                }
            }
            boolean hasMoved = false;
            if (available.length != 0){
                for (int i = 0; i < available.length && !hasMoved; i++){
                    if (available[i] != null && tryMove(available[i])){
                        hasMoved = true;
                        System.out.println("Politician has moved");
                    }
                }

            }
        }

    }

    // Slanderer AI
    static void runSlanderer() throws GameActionException {
        // detect if an enemy is within range
        Team enemy = rc.getTeam().opponent();
        int detectionRadius = rc.getType().detectionRadiusSquared;
        RobotInfo[] threat = rc.senseNearbyRobots(detectionRadius, enemy);
        if (threat.length == 0)
            scatter();
        else
            flee(detectionRadius, threat);

    }

    // Makes the robot move randomly, with a priority for diagonals
    static void scatter() throws GameActionException {
        Direction move_dir = Direction.NORTH;
        for (int i = 0; i < 4; i++) {
            int choice = (int) (Math.random() * 4);
            switch (choice) {
                case 0:
                    move_dir = Direction.NORTHEAST;
                    break;
                case 1:
                    move_dir = Direction.NORTHWEST;
                    break;
                case 2:
                    move_dir = Direction.SOUTHEAST;
                    break;
                case 3:
                    move_dir = Direction.SOUTHWEST;
                    break;
            }
            if (tryMove(move_dir)) {
                return;
            }
        }
        for (int i = 0; i < 4; i++) {
            int choice = (int) (Math.random() * 4);
            switch (choice) {
                case 0:
                    move_dir = Direction.NORTH;
                    break;
                case 1:
                    move_dir = Direction.WEST;
                    break;
                case 2:
                    move_dir = Direction.SOUTH;
                    break;
                case 3:
                    move_dir = Direction.EAST;
                    break;
            }
            if (tryMove(move_dir)) {
                return;
            }
        }
    }

    // Makes the robot run away from threats
    static void flee(int detectionRadius, RobotInfo[] threat) throws GameActionException {
        MapLocation spot = rc.getLocation();
        System.out.println("Threat Detected!");
        int xPos = spot.x;
        int yPos = spot.y;
        int yPriority = 0;
        int xPriority = 0;
        for (RobotInfo robot : threat) {
            MapLocation threatPos = robot.getLocation();
            int threatX = threatPos.x - xPos;
            int threatY = threatPos.y - yPos;
            if (threatX < 0) {
                xPriority -= (detectionRadius - threatX + 1);
            } else if (threatX > 0) {
                xPriority += (detectionRadius - threatX + 1);
            }

            if (threatY < 0) {
                yPriority -= (detectionRadius - threatY + 1);
            } else if (threatY > 0) {
                yPriority += (detectionRadius - threatY + 1);
            }
        }

        if (xPriority == 0) {
            if (yPriority == 0) {
                tryMove(Direction.NORTH);
                return;
            }
            if (yPriority > 0) {
                if (tryMove(Direction.SOUTHWEST))
                    return;
                if (tryMove(Direction.SOUTHEAST))
                    return;
                if (tryMove(Direction.SOUTH))
                    return;
                if (tryMove(Direction.EAST))
                    return;
                if (tryMove(Direction.WEST))
                    return;
            }
            if (yPriority < 0) {
                if (tryMove(Direction.NORTHWEST))
                    return;
                if (tryMove(Direction.NORTHEAST))
                    return;
                if (tryMove(Direction.NORTH))
                    return;
                if (tryMove(Direction.EAST))
                    return;
                if (tryMove(Direction.WEST))
                    return;
            }
        }

        if (xPriority > 0) {
            if (yPriority == 0) {
                if (tryMove(Direction.NORTHWEST))
                    return;
                if (tryMove(Direction.SOUTHWEST))
                    return;
                if (tryMove(Direction.WEST))
                    return;
                if (tryMove(Direction.NORTH))
                    return;
                if (tryMove(Direction.SOUTH))
                    return;
            }
            if (yPriority > 0) {
                if (tryMove(Direction.SOUTHWEST))
                    return;
                if (tryMove(Direction.SOUTH))
                    return;
                if (tryMove(Direction.WEST))
                    return;
            }
            if (yPriority < 0) {
                if (tryMove(Direction.NORTHWEST))
                    return;
                if (tryMove(Direction.NORTH))
                    return;
                if (tryMove(Direction.WEST))
                    return;
            }
        }
        if (xPriority < 0) {
            if (yPriority == 0) {
                if (tryMove(Direction.NORTHEAST))
                    return;
                if (tryMove(Direction.SOUTHEAST))
                    return;
                if (tryMove(Direction.EAST))
                    return;
                if (tryMove(Direction.NORTH))
                    return;
                if (tryMove(Direction.SOUTH))
                    return;
            }
            if (yPriority > 0) {
                if (tryMove(Direction.SOUTHEAST))
                    return;
                if (tryMove(Direction.SOUTH))
                    return;
                if (tryMove(Direction.EAST))
                    return;
            }
            if (yPriority < 0) {
                if (tryMove(Direction.NORTHEAST))
                    return;
                if (tryMove(Direction.NORTH))
                    return;
                if (tryMove(Direction.EAST))
                    return;
            }
        }
    }

    static void runMuckraker() throws GameActionException {
        if (!rc.isReady())
            return;
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;

        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return;
                }
            }
        }

        if (origin == null) {
	    	Team teammate = rc.getTeam();
	    	actionRadius = rc.getType().actionRadiusSquared;
	    	for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, teammate)) {
				if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
		    		origin = robot.getLocation();
				}
	    	}
        }

        leastResistanceStep();
    }
    
    /**
     * Takes a step in the direction with the highest passability
     * Made specifically for Muckrakers, as it deliberately moves away from home (if keeping a home stat is possible??)
     *
     * @return desired Direction for next step (nextDir)
     */
    static Direction leastResistanceStep() throws GameActionException {
    	// get current direction
    	MapLocation location = rc.getLocation(); // get current location
    	Direction awayFromHome = location.directionTo(origin).opposite(); // find direction poiting away from home
    	// need way to keep loc of original enlightenment center ^^ !!


    	// look in all forward/side (not backward) directions for easiest path
    	Direction nextDir = Direction.CENTER;
    	double highestPassability = -1;
    	Direction dir = awayFromHome.rotateLeft().rotateLeft();

    	for (int i=0; i < 4; i++) {
    		MapLocation check = location.add(dir); // location to be checked
    		double passability = rc.sensePassability(check); // looking at passability

    		if (rc.canMove(dir)) {
				if (passability > highestPassability || highestPassability == -1) {
	    			nextDir = dir;
	    			highestPassability = passability;

	    		} else if (passability == highestPassability) { // break passability ties (currently using distance)
		    		int nextDist = location.add(nextDir).distanceSquaredTo(origin);
		    		int dirDist = location.add(dir).distanceSquaredTo(origin);

	    			if (dirDist > nextDist) {
	    				nextDir = dir;
		    			// Idea: choose random direction to keep?
		    			// scala???? idk how to import
	    			}
	    		}
    		}
    		dir = dir.rotateRight(); // check nest direction
    	}


    	// checking if none of the directions are available (ie approached wall/corner)
    	// unlikely this clause will be entered
    	if (highestPassability == -1) {
    		Direction opp = awayFromHome.opposite();
    		Direction leftDir = opp.rotateLeft(), rightDir = opp.rotateRight();

    		if (rc.canMove(leftDir) && rc.canMove(rightDir)) {
	    		double leftDist = location.add(leftDir).distanceSquaredTo(origin);
	    		double rightDist = location.add(rightDir).distanceSquaredTo(origin);

	    		if (leftDist > rightDist) {
	    			nextDir = leftDir;
	    		} else {
	    			nextDir = rightDir;
	    		}
    		} else if (rc.canMove(leftDir) && !rc.canMove(rightDir)) {
    			nextDir = leftDir;
    		} else if (rc.canMove(rightDir) && !rc.canMove(leftDir)) {
    			nextDir = rightDir;
    		} else if (rc.canMove(opp)) {
    			nextDir = opp;
    		} else {
    			nextDir = Direction.CENTER;
    		}
    	}

    	return nextDir;
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        System.out.println("I am trying to move " + dir + "; " + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else
            return false;
    }
}
