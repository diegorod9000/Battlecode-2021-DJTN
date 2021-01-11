package Newcomer;

import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    static final RobotType[] spawnableRobot = { RobotType.POLITICIAN, RobotType.SLANDERER, RobotType.MUCKRAKER, };

    static final Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
            Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };

    static int turnCount;
    static MapLocation origin = null;


    static Team teammate = rc.getTeam();
    static int actionRadius = rc.getType().actionRadiusSquared;
    for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, teammate)) {
        if (robot.type = RobotType.ENLIGHTENMENT_CENTER) {
            static final MapLocation origin = robot.getLocation();
        }
    }	

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

    static void runEnlightenmentCenter() throws GameActionException {
        RobotType toBuild = randomSpawnableRobotType();
        int influence = 50;
        for (Direction dir : directions) {
            if (rc.canBuildRobot(toBuild, dir, influence)) {
                rc.buildRobot(toBuild, dir, influence);
            } else {
                break;
            }
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

        boolean enemyNotDetected = true;

        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                enemyNotDetected = false;
                if (rc.canExpose(robot.location)) {
                    System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return;
                }
            }
        }

        if (origin == null) {
	    static Team teammate = rc.getTeam();
	    static int actionRadius = rc.getType().actionRadiusSquared;
	    for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, teammate)) {
		if (robot.type = RobotType.ENLIGHTENMENT_CENTER) {
		    origin = robot.getLocation();
		}
	    }    	
        }

        // Take a step if Muckraker doesn't find any Politicians to expose
        if (enemyNotDetected) { // Does this need to be (enemyNotDetected == true)??? fuck java
        	leastResistanceStep();
        }
    }
    
    /**
     * Takes a step in the direction with the highest passability
     * Made specifically for Muckrakers, as it deliberately moves away from home (if keeping a home stat is possible??)
     *
     * @return desired Direction for next step (nextDir)
     */
    static void leastResistanceStep() throws GameActionException {
    	// get current direction
    	MapLocation location = rc.getLocation(); // get current location
    	Direction awayFromHome = location.directionTo(HOME).opposite(); // find direction poiting away from home
    	// need way to keep loc of original enlightenment center ^^ !!


    	// look in all forward/side (not backward) directions for easiest path
    	Direction nextDir = new Direction();
    	double highestPassability = null;
    	Direction dir = awayFromHome.rotateLeft().rotateLeft();

    	for (i=0; i < 4; i++) {
    		MapLocation check = location.add(dir); // location to be checked
    		passability = sensePassability(check); // looking at passability

    		if (check.canMove()) {
				if (passability > highestPassability || highestPassability == null) {
	    			nextDir = dir;
	    			highestPassability = passability;

	    		} else if (passability == highestPassability) { // break passability ties (currently using distance)
		    		double nextDist = location.add(nextDir).distanceSquaredTo(origin);
		    		double dirDist = location.add(dirDist).distanceSquaredTo(origin);

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
    	if (lowestPassability == null) {
    		Direction opp = awayFromHome.opposite();
    		Direction leftDir = opp.rotateLeft(), rightDir = opp.rotateRight();

    		if (leftDir.canMove() & rightDir.canMove()) {
	    		double leftDist = location.add(leftDir).distanceSquaredTo(origin);
	    		double rightDist = location.add(rightDir).distanceSquaredTo(origin);

	    		if (leftDist > rightDist) {
	    			nextDir = leftDir;
	    		} else {
	    			nextDir = rightDir;
	    		}
    		} else if (leftDir.canMove() & !rightDir.canMove()) {
    			nextDir = leftDir;
    		} else if (rightDir.canMove() & !leftDir.canMove()) {
    			nextDir = rightDir;
    		} else if (opp.canMove()) {
    			nextDir = opp;
    		} else {
    			nextDir = Direction.CENTER;
    		}
    	}

    	return nextDir
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
