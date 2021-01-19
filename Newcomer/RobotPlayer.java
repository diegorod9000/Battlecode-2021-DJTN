package Newcomer;

import battlecode.common.*;
//import sun.java2d.x11.X11SurfaceDataProxy;

public strictfp class RobotPlayer {
    static RobotController rc;

    static final RobotType[] spawnableRobot = { RobotType.POLITICIAN, RobotType.SLANDERER, RobotType.MUCKRAKER, };

    static final Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
            Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };

    static int turnCount;

    static int homeID = 0;
    static boolean firstTurn = true;

    static MapLocation targetLoc = null;
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
                // System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
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
                // System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }






    static Direction findDirection(int num) {
        if(num>8)
            return null;
        Direction direc = Direction.NORTH;
        switch (num) {
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
        }
        return findDirection(++num);
    }

    static void buildAlternating(int influence) throws GameActionException {
        //builds robots on a 4-round cycle, slanderes are built 2x as much as politicians an muckrakers
        //direction is automatically north but this can be changed, influence as well

        int roundMod = turnCount % 12;
        switch (roundMod) {
            case 0:     buildMuckraker();      break;
            case 2:     
            case 4:     buildPolitician(influence);     break;
            case 6:     buildMuckraker();      break;
            case 10:    buildSlanderer(influence);      break;

        }
    }

    static void buildPolitician(int influence) throws GameActionException {
        Direction direc=findDirection(0);
        if(direc==null)
            return;
        if (rc.canBuildRobot(RobotType.POLITICIAN, direc, influence)) {
            // System.out.println(influence);
            rc.buildRobot(RobotType.POLITICIAN, direc, influence);
            robotCount++;
            // System.out.println("POLITICIAN built on round " + turnCount);
        }
    }

    static void buildSlanderer(int influence) throws GameActionException {
        Direction direc=findDirection(0);
        if(direc==null)
            return;
        if (rc.canBuildRobot(RobotType.SLANDERER, direc, influence)) {
            rc.buildRobot(RobotType.SLANDERER, direc, influence);
            robotCount++;
            // System.out.println("SLANDERER built on round " + turnCount);
        }
    }

    static void buildMuckraker() throws GameActionException {
        Direction direc=findDirection(0);
        int muckInf = 1;
        if(direc==null)
            return;
        if (rc.canBuildRobot(RobotType.MUCKRAKER, direc, muckInf)) {
            rc.buildRobot(RobotType.MUCKRAKER, direc, muckInf);
            robotCount++;
            // System.out.println("MUCKRAKER built on round " + turnCount);
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
        if (rc.senseNearbyRobots(40, rc.getTeam().opponent()).length >= 10) {
            //builds politicians if there are lots of opponents nearby.
            buildPolitician((int)(Math.round(rc.getInfluence() * calcLinearBuildPercent())));
        } else {
            buildAlternating((int)(Math.round(rc.getInfluence() * calcLinearBuildPercent())));
        }

        //bidding

        if (turnCount <= 300) {
            if (rc.canBid(1))
                rc.bid(1);
        } else if (turnCount > GameConstants.GAME_MAX_NUMBER_OF_ROUNDS - 25) {
            rc.bid(rc.getInfluence());
//        } else if (turnCount > GameConstants.GAME_MAX_NUMBER_OF_ROUNDS * 2/3) {
//            rc.bid(10);
        } else {
            bidPercent(calcQuadBidPercent());
        }
        
    }


    static void sendMovingFlag() throws GameActionException {
        //for moving robots if they see an enemy, reports the enemy's location and ID in flag

        RobotInfo[] enemyBotInfo = rc.senseNearbyRobots(20, rc.getTeam().opponent());
        MapLocation location = new MapLocation(0,0);
        int intID = 0;
        boolean found = false;
        boolean inBounds = true;

        if (enemyBotInfo.length != 0) {
            //sends flag if there are enemies detected

            for (RobotInfo info: enemyBotInfo) {
                //if any opponent is an enlightenment center, set ID to 0 or 1023 based on team
                if (info.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    if (info.getTeam() == Team.NEUTRAL) {
                        intID = 0;
                    } else {
                        intID = 1023;
                    }
                    location = info.getLocation();
                    found = true;
                }
            }

            if (!found) {
                //if no center found and if the ID is between 1 and 1022, set ID and location
                for (RobotInfo info: enemyBotInfo) {
                    if (info.getID() % 1024 == 0 || info.getID() % 1024 == 1023) {
                        inBounds = false;
                    }
                }

                if (inBounds) {
                    intID = enemyBotInfo[0].getID() % 1024;
                    location = enemyBotInfo[0].getLocation();
                }
            }

            if (inBounds) {
                //sends flag if the ID is between 1 and 1022, because 0 and 1023 are for E-Centers
                int x = location.x, y = location.y;
                int encodedLocation = 0;
                //((intID & BITMASK) << 2 * NBITS) + ((x & BITMASK) << NBITS) + (y & BITMASK);
                if (rc.canSetFlag(encodedLocation)) {
                    rc.setFlag(encodedLocation);
                }
            }
        }
    }


    //run at top of code (runs first turn and records id of home EC
    static void getHomeECID() throws GameActionException{
        if(!firstTurn){
            return;
        }
        Team friendly = rc.getTeam();
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        RobotInfo nearestEC = null;

        System.out.println("Nearby Robots: " + nearbyRobots.length);
        for(int i = 0; i < nearbyRobots.length;i++){
            if(nearestEC == null && nearbyRobots[i].getTeam().equals(friendly) && nearbyRobots[i].getType().equals(RobotType.ENLIGHTENMENT_CENTER)){
                nearestEC = nearbyRobots[i];
            }
            else if(nearestEC != null && nearbyRobots[i].getType().equals(RobotType.ENLIGHTENMENT_CENTER)){
                if(nearbyRobots[i].getLocation().distanceSquaredTo(rc.getLocation()) < nearestEC.getLocation().distanceSquaredTo(rc.getLocation())){
                    nearestEC = nearbyRobots[i];
                }
            }
        }

        firstTurn = false;
        homeID = nearestEC.getID();
//        System.out.println("My home ID is " + homeID);

    }

    //pathfinding Algorithm to move toward targetLoc variable
    static void polPathFind() throws GameActionException{
        Direction targetDir = rc.getLocation().directionTo(targetLoc);
        Direction[] availDirs = new Direction[3];
        availDirs[1] = targetDir;

        //get index in directions array
        int dirIndex = 0;
        for (int i = 0; i < directions.length;i++){
            if(directions[i].equals(targetDir)){
                dirIndex = i;
            }
        }

        //fill in adjacent directions
        if(dirIndex >= 1){
            availDirs[0] = directions[dirIndex - 1];
        }else{
            availDirs[0] = directions[directions.length - 1];
        }

        if(dirIndex <= directions.length - 2){
            availDirs[2] = directions[dirIndex + 1];
        }else{
            availDirs[2] = directions[0];
        }

        //find the highest passability amoung possible directions
        double max = 0.0;
        Direction bestDir = null;
        for(int i = 0; i < availDirs.length;i++){
            double currentPass = rc.sensePassability(rc.getLocation().add(availDirs[i]));
            if (currentPass > max){
                bestDir = availDirs[i];
                max = currentPass;
            }
        }

        //if max is <= .1 (all directions have bad passability), move perpendicular
        if(max <= .1){
            if(dirIndex >= 2){
                bestDir = directions[dirIndex-2];
            }
            else if (dirIndex == 1){
                bestDir = directions[directions.length-1];
            }
            else{
                bestDir = directions[directions.length-2];
            }
        }

        //move in that direction
        if(rc.canMove(bestDir)){
            rc.move(bestDir);
        }

    }

    //detects if on a mission, if not checks home flag
    static boolean onMission() throws GameActionException{
        if (targetLoc == null){
            polCheckHomeFlag();
            if(targetLoc == null){
                return false;
            }
        }
        return true;
    }

    //checks if arrived at target, if so empowers and then ends mission (targetLoc = null)
    static boolean arrived(int actionRadius) throws GameActionException{
        if(rc.getLocation().distanceSquaredTo(targetLoc) < actionRadius){
            if(rc.canEmpower(actionRadius)){
                rc.empower(actionRadius);
                targetLoc = null;
                return true;
            }
        }
        return false;
    }

    //checks home flag and gets any relevant information from it
    static void polCheckHomeFlag() throws GameActionException{
        //System.out.println("Home ID:" + homeID);
        int flag = rc.getFlag(homeID);

        //IMPLEMENT CODE TO GET FLAG INFORMATION

    }


    //Politician AI
    static void runPolitician() throws GameActionException {
        sendMovingFlag();
        //basic variables
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        MapLocation[] enemies = rc.detectNearbyRobots();

        //only runs on the first turn
        getHomeECID();

        //checks if on mission
        if (onMission() && !arrived(actionRadius)){
            polPathFind();
            return;
        }

        boolean winning = true;
        boolean nearHome = false;


        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        RobotInfo[] nuetralECs = rc.senseNearbyRobots(actionRadius, Team.NEUTRAL);

        if(nuetralECs.length != 0){
            if(rc.canEmpower(actionRadius)){
                rc.empower(actionRadius);
                // System.out.println("Empowering near Neutral Enlightenment Center");
            }
        }

        // Checking if within EC radius
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, rc.getTeam())) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                nearHome = true;
            }
        }

        // System.out.println("" + rc.getTeamVotes() + " " + rc.getRoundNum());
        double rand = Math.random();
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            if (nearHome || rand <= .1) {
                // System.out.println("empowering...");
                rc.empower(actionRadius);
                // System.out.println("empowered");
                return;
            }
        } else {
            winning = false;
            //System.out.println("We are losing badly so I will not attack");
        }

        if (winning || attackable.length == 0){
            if (tryMove(randomDirection())){
                // System.out.println("I moved!");
            }
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
                        // System.out.println("Politician has moved");
                    }
                }

            }
        }

    }

    // Slanderer AI
    static void runSlanderer() throws GameActionException {

        sendMovingFlag();

        if (!rc.isReady()) {
            return;
        }
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
        Direction diag = null;
        Direction straight = null;
        int overflow=0;
        while(diag==null && overflow<5){
            overflow++;
            int choice = (int) (Math.random() * 4);
            switch (choice) {
                case 0:
                    if(rc.canMove(Direction.NORTHEAST))
                        diag = Direction.NORTHEAST;
                    break;
                case 1:
                    if(rc.canMove(Direction.NORTHWEST))
                        diag = Direction.NORTHWEST;
                    break;
                case 2:
                    if(rc.canMove(Direction.SOUTHEAST))
                        diag = Direction.SOUTHEAST;
                    break;
                case 3:
                    if(rc.canMove(Direction.SOUTHWEST))
                        diag = Direction.SOUTHWEST;
                    break;
            }
        }
        overflow=0;
        while(straight==null && overflow<5){
            overflow++;
            int choice = (int) (Math.random() * 4);
            switch (choice) {
                case 0:
                    if(rc.canMove(Direction.NORTH))
                        straight = Direction.NORTH;
                    break;
                case 1:
                    if(rc.canMove(Direction.WEST))
                        straight = Direction.WEST;
                    break;
                case 2:
                    if(rc.canMove(Direction.EAST))
                        straight = Direction.EAST;
                    break;
                case 3:
                    if(rc.canMove(Direction.SOUTH))
                        straight = Direction.SOUTH;
                    break;
            }
        }
        if(diag==null){
            if(straight==null){
                return;
            }
            else{
                rc.move(straight);
            }
        }
        else if(straight==null){
            rc.move(diag);
        }
        else{
            MapLocation position=rc.getLocation();
            MapLocation diagPath=position.add(diag);
            MapLocation strPath=position.add(straight);
            if(rc.sensePassability(diagPath)<(rc.sensePassability(strPath)/1.3)){
                rc.move(straight);
            }
            else {
                rc.move(diag);
            }
        }

    }

    // Makes the robot run away from threats
    static void flee(int detectionRadius, RobotInfo[] threat) throws GameActionException
    {
        MapLocation spot = rc.getLocation();
        // System.out.println("Threat Detected!");
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
        Direction[] paths;
        if (xPriority == 0) {
            if (yPriority == 0) {
                tryMove(Direction.NORTH);
                return;
            } else if (yPriority > 0) {
                paths= new Direction[]{Direction.SOUTHEAST,Direction.SOUTHWEST,Direction.SOUTH,Direction.EAST,Direction.WEST};
            } else {
                paths=new Direction[]{Direction.NORTHEAST,Direction.NORTHWEST,Direction.NORTH,Direction.EAST,Direction.WEST};
            }
        } else if (xPriority > 0) {
            if (yPriority == 0) {
                paths=new Direction[]{Direction.NORTHWEST,Direction.SOUTHWEST,Direction.WEST,Direction.NORTH,Direction.SOUTH};
            } else if (yPriority > 0) {
                paths=new Direction[]{Direction.SOUTHWEST,Direction.SOUTH,Direction.WEST};
            } else {
                paths=new Direction[]{Direction.NORTHWEST,Direction.NORTH,Direction.WEST};
            }
        } else {
            if (yPriority == 0) {
                paths=new Direction[]{Direction.NORTHEAST,Direction.SOUTHEAST,Direction.EAST,Direction.NORTH,Direction.SOUTH};
            } else if (yPriority > 0) {
                paths=new Direction[]{Direction.SOUTHEAST,Direction.SOUTH,Direction.EAST};
            } else {
                paths=new Direction[]{Direction.NORTHEAST,Direction.NORTH,Direction.EAST};
            }
        }
        MapLocation position = rc.getLocation();
        if (paths.length == 5) {
            double diag = 0;
            int diagTracker=-1;
            double away = 0;
            double side = 0;
            int sideTracker=-1;

            if (rc.canMove(paths[2])) {
                away = rc.sensePassability(position.add(paths[2]));
            }
            if (rc.canMove(paths[0])) {
                if (rc.canMove(paths[1])) {
                    double pass1=rc.sensePassability(position.add(paths[0]));
                    double pass2=rc.sensePassability(position.add(paths[1]));
                    if(pass1>pass2){
                        diag=pass1;
                        diagTracker=0;
                    }
                    else{
                        diag=pass2;
                        diagTracker=1;
                    }
                }
                else{
                    diag = rc.sensePassability(position.add(paths[0]));
                    diagTracker = 0;
                }
            }
            else if(rc.canMove(paths[1])){
                diag = rc.sensePassability(position.add(paths[1]));
                diagTracker=1;
            }

            if (rc.canMove(paths[3])) {
                if (rc.canMove(paths[4])) {
                    double pass1=rc.sensePassability(position.add(paths[3]));
                    double pass2=rc.sensePassability(position.add(paths[4]));
                    if(pass1>pass2){
                        side=pass1;
                        sideTracker=3;
                    }
                    else{
                        side=pass2;
                        sideTracker=4;
                    }
                }
                else{
                    side = rc.sensePassability(position.add(paths[3]));
                    sideTracker = 3;
                }
            }
            else if(rc.canMove(paths[4])){
                side = rc.sensePassability(position.add(paths[4]));
                sideTracker=4;
            }

            if(diag<away/1.3&&away>0){
                if(away<side/1.4){
                    rc.move(paths[sideTracker]);
                }
                else{
                    rc.move(paths[2]);
                }
            }
            else if(diag<side/2.0&&side>0){
                rc.move(paths[sideTracker]);
            }
            else if(diag>0){
                rc.move(paths[diagTracker]);
            }
        }
        else {
            double diag = 0;
            double straight = 0;
            int straightTracker = -1;
            if (rc.canMove(paths[0])) {
                diag = rc.sensePassability(position.add(paths[0]));
            }
            if (rc.canMove(paths[1])) {
                if (rc.canMove(paths[2])) {
                    double pass1=rc.sensePassability(position.add(paths[1]));
                    double pass2=rc.sensePassability(position.add(paths[2]));
                    if(pass1>pass2){
                        straight=pass1;
                        straightTracker=1;
                    }
                    else{
                        straight=pass2;
                        straightTracker=2;
                    }
                }
                else{
                    straight = rc.sensePassability(position.add(paths[1]));
                    straightTracker = 1;
                }
            }
            else if(rc.canMove(paths[2])){
                straight = rc.sensePassability(position.add(paths[2]));
                straightTracker=2;
            }


            if (diag<straight/1.3&&straightTracker>0) {
                rc.move(paths[straightTracker]);
            }else if(diag>0){
                rc.move(paths[0]);
            }

        }
    }



    static void runMuckraker() throws GameActionException {

        sendMovingFlag();

        if (!rc.isReady())
            return;
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;

        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    // System.out.println("e x p o s e d");
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

        wallBounce();
    }


    static Direction currentDirection = null;
    static Direction altDirection;
    static boolean stepnum = true;

    static Direction getDirection(int dx, int dy) throws GameActionException{
        MapLocation l1 = rc.getLocation();
        return l1.directionTo(l1.translate(dx, dy));
    }

    /**
     * Takes a step in a direction until it hits a wall
     * Made for Muckrakers
     *
     * No return value, takes a step
     */
    static void wallBounce() throws GameActionException {
        // Set initial Direction away from E-center
        if (currentDirection == null) {
            MapLocation location = rc.getLocation(); // get current location
            Direction d1 = location.directionTo(origin).opposite();

            if (Math.abs(d1.getDeltaX()) + Math.abs(d1.getDeltaY()) == 1) {
                currentDirection = d1; // cardinal direction
                altDirection = d1.rotateRight(); // diagonal direction
            } else {
                altDirection = d1;
                currentDirection = d1.rotateRight();
            }
        }

        // set step alternating step direction
        Direction stepdir = (stepnum) ? currentDirection : altDirection;
        MapLocation stepspot = rc.adjacentLocation(stepdir);

        if (!rc.onTheMap(stepspot)) {
            int ax = altDirection.getDeltaX(), ay = altDirection.getDeltaY();
            if (currentDirection.getDeltaX() == 0) {
                currentDirection = currentDirection.opposite();
                altDirection = getDirection(ax, -ay);
            } else {
                currentDirection = currentDirection.opposite();
                altDirection = getDirection(-ax, ay);
            }
        }

        Direction randir = randomDirection();
        if (rc.canMove(stepdir))
            rc.move(stepdir);
        else if (rc.canMove(randir))
            rc.move(randir);

        stepnum = !stepnum;
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
        // System.out.println("I am trying to move " + dir + "; " + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else
            return false;
    }
}
