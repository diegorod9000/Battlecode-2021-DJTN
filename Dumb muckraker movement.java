    static MapLocation origin = null;
    
    /**
     * Takes a step in the direction with the highest passability
     * Made specifically for Muckrakers, as it deliberately moves away from home (if keeping a home stat is possible??)
     *
     * No return value, just moves
     */
    static void leastResistanceStep() throws GameActionException {
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

    	rc.move(nextDir);
    }
