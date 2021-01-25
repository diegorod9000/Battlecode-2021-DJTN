package Newcomer;

import battlecode.common.*;
//import sun.java2d.x11.X11SurfaceDataProxy;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

public strictfp class RobotPlayer {
    static RobotController rc;

    static final RobotType[] spawnableRobot = {RobotType.POLITICIAN, RobotType.SLANDERER, RobotType.MUCKRAKER,};

    static final Direction[] directions = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
            Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST,};

    static int turnCount;

    static int homeID = 0;
    static boolean firstTurn = true;

    static MapLocation targetLoc = null;
    static MapLocation origin = null;

    static ArrayList<Integer> childIDs = new ArrayList<>();

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

        // System.out.println("I'm a " + rc.getType() + " and I just got created!");

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


    static final int NBITS = 7;
    static final int BITMASK = (1 << NBITS) - 1;
    static final int BITMASKID = (1 << 10) - 1;
    static ArrayList<Integer> friendlyIDs = new ArrayList<Integer>();
    static ArrayList<Integer> allFlags = new ArrayList<Integer>();


    static void getAndSendFlags() throws GameActionException {

        //adds new friendly robots
        RobotInfo[] AllFriendsSeen = rc.senseNearbyRobots(1, rc.getTeam());
        for (RobotInfo info : AllFriendsSeen) {
            if (!friendlyIDs.contains(info.getID())) {
                friendlyIDs.add(info.getID());
            }
        }

        //deletes dead robots
//        for (int index = 0; index < friendlyIDs.size(); index++) {
//            if (!rc.canGetFlag(friendlyIDs.get(index))) {
//                friendlyIDs.remove(index);
//                index--;
//            }
//        }

        ArrayList<Integer> newFlags = new ArrayList<Integer>();
        for (Integer ID : friendlyIDs) {
            if (rc.canGetFlag(ID)) {
                if (rc.getFlag(ID) != 0) {
                    newFlags.add(rc.getFlag(ID));
                }
            }
        }


        for (Integer newFlag : newFlags) {
            if(decodeFlagDefeated(newFlag)){
                Integer flagToRemove = encodeFlag(decodeFlagLocation(newFlag), decodeFlagTeam(newFlag), false);
                if(allFlags.contains(flagToRemove)){
                    allFlags.remove(flagToRemove);
                }
            }
            else if (!allFlags.contains(newFlag)) {
                allFlags.add(newFlag);
            }
        }

        allFlags = sortFlags(allFlags);


        if (allFlags.size() > 0) {
            System.out.println(decodeFlagLocation(allFlags.get(0)).toString());
            if (rc.canSetFlag(allFlags.get(0).intValue())) {
                rc.setFlag(allFlags.get(0).intValue());
            }
        }
        else{
            int cancelFlag = 9999999;
            if(rc.canSetFlag(cancelFlag)){
                rc.setFlag(cancelFlag);
            }
        }
    }

    static ArrayList<Integer> sortFlags(ArrayList<Integer> allFlags) throws GameActionException {
        ArrayList<Integer> nuetralECs = new ArrayList<>();
        ArrayList<Integer> enemyECs = new ArrayList<>();

        for(int i = 0; i < allFlags.size(); i++){
            if(decodeFlagTeam(allFlags.get(i)) == Team.NEUTRAL){
                nuetralECs.add(allFlags.get(i));
            }
            else if(decodeFlagTeam(allFlags.get(i)) == rc.getTeam().opponent()){
                enemyECs.add(allFlags.get(i));
            }
        }

        nuetralECs = sortByDistance(nuetralECs);
        enemyECs = sortByDistance(enemyECs);

        for(int i = 0; i < enemyECs.size();i++){
            nuetralECs.add(enemyECs.get(i));
        }

        return nuetralECs;
    }

    static ArrayList<Integer> sortByDistance (ArrayList<Integer> array) throws GameActionException{
        for (int i = 1; i < array.size(); i++) {
            int current = decodeFlagLocation(array.get(i)).distanceSquaredTo(rc.getLocation());
            int j = i - 1;
            while(j >= 0 && current < decodeFlagLocation(array.get(j)).distanceSquaredTo(rc.getLocation())) {
                array.set(j+1, array.get(j));
                j--;
            }
            // at this point we've exited, so j is either -1
            // or it's at the first element where current >= a[j]
            array.set(j+1,current);
        }
        return array;
    }

    static Direction findDirection(int num) {
        if (num > 8)
            return null;
        Direction direc = Direction.NORTH;
        switch (num) {
            case 0:
                direc = Direction.NORTH;
                break;
            case 1:
                direc = Direction.SOUTH;
                break;
            case 2:
                direc = Direction.EAST;
                break;
            case 3:
                direc = Direction.WEST;
                break;
            case 4:
                direc = Direction.NORTHEAST;
                break;
            case 5:
                direc = Direction.SOUTHWEST;
                break;
            case 6:
                direc = Direction.NORTHWEST;
                break;
            case 7:
                direc = Direction.SOUTHEAST;
                break;
        }

        if (rc.canBuildRobot(RobotType.POLITICIAN, direc, 1)) {
            return direc;
        }
        return findDirection(++num);
    }

    static void buildAltSSPM(int influence) throws GameActionException {
        //builds robots on a cycle, slanderes are built 2x as much as politicians an muckrakers

        int roundMod = rc.getRoundNum() % 18;
        switch (roundMod) {
            case 0:
            case 1:
                buildSlanderer(influence);
                break;
            case 2:
            case 3:
                buildPolitician(influence);
                break;
            case 4:
            case 5:
                buildPolitician(influence);
                break;
            case 6:
            case 7:
                buildMuckraker();
                break;
            case 8:
            case 9:
                buildSlanderer(influence);
                break;
            case 10:
            case 11:
                buildPolitician(influence);
                break;
            case 12:
            case 13:
                buildSlanderer(influence);
                break;
            case 14:
            case 15:
                buildPolitician(influence);
                break;
            case 16:
            case 17:
                buildSlanderer(influence);
                break;
        }
    }

    static int lastRoundInf = 0;

    static void bidAndBuild() throws GameActionException {
        int roundNum = 	rc.getRoundNum();
        int myVotes = rc.getTeamVotes();


        int infToUse = lastRoundInf - rc.getInfluence();
        int toChange = 0;
        double percentMade = infToUse * 1.0 / lastRoundInf;

        if (percentMade > 1) {
            toChange = -1 * (int)(infToUse * 0.25); //save
        } else if (percentMade < 0.05) {
            toChange = (int)(lastRoundInf * 0.1);  //take
        }
        infToUse += toChange;

        double percentBid = 0;
        if (roundNum < 500)
            percentBid = 0.25;
        else if (roundNum < 750)
            percentBid = 0.35;
        else if (roundNum < 1000)
            percentBid = 0.45;
        else if (roundNum < 1250)
            percentBid = 0.5;



        if (!(myVotes > 750 || (GameConstants.GAME_MAX_NUMBER_OF_ROUNDS - roundNum) < (751 - myVotes))) {
            if (roundNum < 300) {
                if (rc.canBid(1))
                    rc.bid(1);
            } else if (roundNum < 1250) {
                if (rc.canBid((int)(infToUse * percentBid)))
                    rc.bid((int)(infToUse * percentBid));
                else
                    rc.bid((int)(rc.getInfluence() * 0.1));
            } else {
                rc.bid((int)(rc.getInfluence() * 0.40));
            }
        }

        RobotInfo[] enemiesNearby = rc.senseNearbyRobots(rc.getType().detectionRadiusSquared, rc.getTeam().opponent());
        if (roundNum < 300) {
            if (enemiesNearby.length > 5 || (roundNum >= 15 && roundNum <= 25))
                buildPolitician((int)(Math.round(rc.getInfluence() * 0.3)));
            else
                buildEarly();
        } else if (roundNum < 1200) {
            if (enemiesNearby.length > 15)
                buildPolitician((int)(Math.round(rc.getInfluence() * 0.3)));
            else
                buildAltSSPM((int)(infToUse * (1 - percentBid)));
        }
    }

    static void buildEarly() throws GameActionException {
        // builds initial robots in specified manner
        // slanderers and muckrakers only
        if (rc.getRoundNum() < 50) {
            if (rc.getInfluence() >= 130)
                buildSlanderer(130);
        } else {
            if (rc.getInfluence() >= 532)
                buildSlanderer(532);
            else
                buildMuckraker();
        }
    }

    static void buildPolitician(int influence) throws GameActionException {
        Direction direc = findDirection(0);
        if (direc == null)
            return;
        if (rc.canBuildRobot(RobotType.POLITICIAN, direc, influence)) {
            // System.out.println(influence);
            rc.buildRobot(RobotType.POLITICIAN, direc, influence);
            robotCount++;
            // System.out.println("POLITICIAN built on round " + turnCount);
        }
    }

    static void buildSlanderer(int influence) throws GameActionException {
        Direction direc = findDirection(0);
        if (direc == null)
            return;
        if (rc.canBuildRobot(RobotType.SLANDERER, direc, influence)) {
            rc.buildRobot(RobotType.SLANDERER, direc, influence);
            robotCount++;
            // System.out.println("SLANDERER built on round " + turnCount);
        }
    }

    static void buildMuckraker() throws GameActionException {
        Direction direc = findDirection(0);
        int muckInf = 1;
        if (direc == null)
            return;
        if (rc.canBuildRobot(RobotType.MUCKRAKER, direc, muckInf)) {
            rc.buildRobot(RobotType.MUCKRAKER, direc, muckInf);
            robotCount++;
            // System.out.println("MUCKRAKER built on round " + turnCount);
        }
    }

    static int robotCount;

    static void runEnlightenmentCenter() throws GameActionException {

        //bidding and building
        bidAndBuild();

        //flags
        getAndSendFlags();
    }



    // FLAGS FLAGS FLAGS FLAGS FLAGS FLAGS FLAGS FLAGS FLAGS FLAGS FLAGS FLAGS FLAGS FLAGS FLAGS
    static void sendMovingFlag() throws GameActionException {
        //for moving robots if they see an enemy, reports the enemy's location and ID in flag

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(rc.getType().detectionRadiusSquared);
        int intID = 0;

        if (nearbyRobots.length == 0) {
            //sends no flag if there are no enemies detected
            return;
        }

        boolean isDominated = false;
        if (homeID != 0)
            if (rc.canSenseLocation(decodeFlagLocation(rc.getFlag(homeID)))){
                if(rc.senseRobotAtLocation(decodeFlagLocation(rc.getFlag(homeID))) != null){
                    if (rc.senseRobotAtLocation(decodeFlagLocation(rc.getFlag(homeID))).getTeam() == rc.getTeam())
                        isDominated = true;
                }
            }

        int flagToBeSet = 0;
        for(int i = 0; i < nearbyRobots.length;i++){
            if(nearbyRobots[i].getType() == RobotType.ENLIGHTENMENT_CENTER && nearbyRobots[i].getTeam() != rc.getTeam()){
                flagToBeSet = encodeFlag(nearbyRobots[i].getLocation(),nearbyRobots[i].getTeam(), isDominated);
                if(rc.canSetFlag(flagToBeSet)) {
                    rc.setFlag(flagToBeSet);
                    return;
                }
            }
        }
        rc.setFlag(0);
    }

    // encodes location and team (and future items if necessary) into flag
    static int encodeFlag(MapLocation target, Team team, boolean isDominated) throws GameActionException {
        int location = 128 * (target.y % 128) + target.x % 128;
        int teamType = 128 * 128 * ((team == rc.getTeam().opponent()) ? 1 : 0);
        int changeLocation = 128 * 128 * 2 * ((isDominated) ? 1 : 0);
        return location + teamType + changeLocation;
    }

    // decodes flag's location (not to be confused with team)
    static MapLocation decodeFlagLocation(int flag) throws GameActionException {
        MapLocation loc = rc.getLocation();

        int xDiff = (flag % 128) - (loc.x % 128);
        int xMultiplier = (xDiff < 0) ? 1 : -1;
        int delX = (Math.abs(xDiff) < 64) ? xDiff : xDiff + xMultiplier * 128;
        int x = loc.x + delX;

        int yDiff = ((flag / 128) % 128) - (loc.y % 128);
        int yMultiplier = (yDiff < 0) ? 1 : -1;
        int delY = (Math.abs(yDiff) < 64) ? yDiff : yDiff + yMultiplier * 128;
        int y = loc.y + delY;

        return new MapLocation(x, y);
    }

    // decodes flag's team (not to be confused with location)
    static Team decodeFlagTeam (int flag) throws GameActionException {
        int teamType = (flag / (128 * 128)) % 2;
        return (teamType == 1) ? rc.getTeam().opponent() : Team.NEUTRAL;
    }

    static boolean decodeFlagDefeated (int flag) throws GameActionException {
        int defeated = (flag / (128 * 128 * 2)) % 2;
        return (defeated == 1);
    }

    static boolean decodeFlagGoHome (int flag) throws GameActionException {
        int gohome = (flag / (128 * 128 * 2 * 2)) % 2;
        return (gohome == 1);
    }
    // No more flags



    //run at top of code (runs first turn and records id of home EC
    //run at top of code (runs first turn and records id of home EC
    static void getHomeECID () throws GameActionException {
//         System.out.println(homeID);
        if (!firstTurn) {
            return;
        }

        Team friendly = rc.getTeam();
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(rc.getType().detectionRadiusSquared);
        RobotInfo nearestEC = null;

//         System.out.println("Nearby Robots: " + nearbyRobots.length);
        if (nearbyRobots.length == 0) {
            return;
        }
        for (int i = 0; i < nearbyRobots.length; i++) {
            if (nearestEC == null && nearbyRobots[i].getTeam().equals(friendly) && nearbyRobots[i].getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
                nearestEC = nearbyRobots[i];
            } else if (nearestEC != null && nearbyRobots[i].getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
                if (nearbyRobots[i].getLocation().distanceSquaredTo(rc.getLocation()) < nearestEC.getLocation().distanceSquaredTo(rc.getLocation())) {
                    nearestEC = nearbyRobots[i];
                }
            }
        }

        firstTurn = false;
        if (nearestEC != null) {
            homeID = nearestEC.getID();
        }
    }

    //pathfinding Algorithm to move toward targetDir (changed to pass in direction)
    static boolean pathfind (Direction targetDir) throws GameActionException {
        Direction[] availDirs = new Direction[3];
        availDirs[1] = targetDir;

        //get index in directions array
        int dirIndex = 0;
        for (int i = 0; i < directions.length; i++) {
            if (directions[i].equals(targetDir)) {
                dirIndex = i;
            }
        }

        //fill in adjacent directions
        if (dirIndex >= 1) {
            availDirs[0] = directions[dirIndex - 1];
        } else {
            availDirs[0] = directions[directions.length - 1];
        }

        if (dirIndex <= directions.length - 2) {
            availDirs[2] = directions[dirIndex + 1];
        } else {
            availDirs[2] = directions[0];
        }

        //find the highest passability amoung possible directions
        double max = 0.0;
        double[] allPasses = new double[3];

        Direction bestDir = null;
        for (int i = 0; i < availDirs.length; i++) {
            double currentPass=0.0;
            if(rc.canSenseLocation(rc.getLocation().add(availDirs[i]))){
                currentPass = rc.sensePassability(rc.getLocation().add(availDirs[i]));
            }
            allPasses[i] = currentPass;
            if (currentPass > max) {
                bestDir = availDirs[i];
                max = currentPass;
            }
        }

        //if max is <= .1 (all directions have bad passability), move perpendicular
        if (max <= .1) {
            if (dirIndex >= 2) {
                bestDir = directions[dirIndex - 2];
            } else if (dirIndex == 1) {
                bestDir = directions[directions.length - 1];
            } else {
                bestDir = directions[directions.length - 2];
            }
        }

        //if all have same passability, move in target direction
        if(allPasses[0] == allPasses[1] && allPasses[1] == allPasses[2]){
            if(rc.canMove(targetDir)){
                rc.move(targetDir);
                return true;
            }
        }

        //move in that direction
        if (rc.canMove(bestDir)) {
            rc.move(bestDir);
            return true;
        }
        else if(rc.canMove(targetDir)){
            rc.move(targetDir);
            return true;
        }
        return false;

    }

    //detects if on a mission, if not checks home flag
    static boolean onMission () throws GameActionException {
        if (targetLoc == null) {
            polCheckHomeFlag();
            if (targetLoc == null) {
                return false;
            }
        }
        return true;
    }

    //checks if arrived at target, if so empowers and then ends mission (targetLoc = null)
    static boolean arrived (int actionRadius) throws GameActionException {

        //NEED TO MAKE SURE ITS STILL NUETRAL
        if (rc.getLocation().distanceSquaredTo(targetLoc) < actionRadius) {
            if (rc.senseNearbyRobots(targetLoc, actionRadius, Team.NEUTRAL).length > 0) {
                if (rc.canEmpower(actionRadius)) {
                    rc.empower(actionRadius);
                }
            }
            targetLoc = null;
            return true;
        }
        return false;
    }

    //checks home flag and gets any relevant information from it
    static void polCheckHomeFlag () throws GameActionException {
        int flag = rc.getFlag(homeID);

        if (decodeFlagGoHome(flag)){
            targetLoc = decodeFlagLocation(flag);
        } else if (!decodeFlagDefeated(flag)) {
            targetLoc = decodeFlagLocation(flag);
        }

    }


    //Politician AI
    static void runPolitician () throws GameActionException {

        sendMovingFlag();

        //basic variables
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        MapLocation[] enemies = rc.detectNearbyRobots();

        //only runs on the first turn
        getHomeECID();

        //checks if on mission

        if(rc.getRoundNum() == 1475){
            if(rc.canEmpower(actionRadius)){
                rc.empower(actionRadius);
            }
        }



        boolean winning = true;
        boolean nearHome = false;


        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        RobotInfo[] neutralECs = rc.senseNearbyRobots(actionRadius, Team.NEUTRAL);

        for (RobotInfo id : attackable) {
            if (id.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                if (rc.canEmpower(actionRadius)) {
                    rc.empower(actionRadius);
                }
            }
        }

        if (neutralECs.length != 0) {
            if (rc.canEmpower(actionRadius)) {
                rc.empower(actionRadius);
            }
        }

        //set target location = to whatever home is broadcasting
        if(rc.canGetFlag(homeID)){
            int flag = rc.getFlag(homeID);
            if(flag == 9999999){
                targetLoc = null;
            }
            else if (flag != 0){
                polCheckHomeFlag();
            }
        }

        if(targetLoc != null && !arrived(actionRadius)){
//             System.out.print(targetLoc.toString());
            pathfind(rc.getLocation().directionTo(targetLoc));
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
            winning = true;
            //System.out.println("We are losing badly so I will not attack");
        }

        if (winning || attackable.length == 0) {
            if (tryMove(randomDirection())) {
                // System.out.println("I moved!");
            }
        } else {
            Direction[] available = new Direction[8];
            for (int i = 0; i < attackable.length && i < 8; i++) {
                if (attackable[i].getTeam().equals(rc.getTeam().opponent())) {
                    available[i] = attackable[i].getLocation().directionTo(rc.getLocation());
                }
            }
            boolean hasMoved = false;
            if (available.length != 0) {
                for (int i = 0; i < available.length && !hasMoved; i++) {
                    if (available[i] != null && tryMove(available[i])) {
                        hasMoved = true;
                        // System.out.println("Politician has moved");
                    }
                }

            }
        }

    }

    // Slanderer AI
    static RobotInfo[] rememberThreat;
    static int trackRemainingTurns = 300;
    static MapLocation slanderHome = null;
    static void runSlanderer () throws GameActionException {
        trackRemainingTurns--;
        if (!rc.isReady()) {
            return;
        }
        sendMovingFlag();
        int self_id=rc.getID();
        // detect if an enemy is within range
        Team color = rc.getTeam();
        MapLocation position = rc.getLocation();

        int detectionRadius = rc.getType().detectionRadiusSquared;
        RobotInfo[] friendly = rc.senseNearbyRobots(detectionRadius, color);
        RobotInfo[] threat = rc.senseNearbyRobots(detectionRadius, color.opponent());

        HashSet<MapLocation> locations= new HashSet<MapLocation>();

        for (RobotInfo friend: friendly){
            int id=friend.getID();
            if(rc.canGetFlag(id)&&rc.getFlag(id)!=0){
                locations.add(decodeFlagLocation(rc.getFlag(id)));
            }

            if(friend.getType()==RobotType.ENLIGHTENMENT_CENTER){
                slanderHome=friend.getLocation();
            }
            //Gets out of the way if it's too close to an EC
            if(friend.getType()==RobotType.ENLIGHTENMENT_CENTER&&friend.getLocation().isWithinDistanceSquared(position,2)){
                locations.add(friend.getLocation());
            }
        }

        if (threat.length!= 0){
            //runs away from immediate threat and reembers it for 1 turn
            flee(threat);
            rememberThreat=threat;
        }
        else if(slanderHome!=null&&slanderHome.distanceSquaredTo(position)>(int)(trackRemainingTurns*1.1)&&slanderHome.distanceSquaredTo(position)>=detectionRadius)
        {
//             System.out.println("Going home");
            if(pathfind(position.directionTo(slanderHome)))
            {
//                 System.out.println("moved");
            }
        }
        else if(locations.size()!=0){
            //avoids dangerous locations
            if(rc.canGetFlag(self_id)&&rc.getFlag(self_id)!=0){
                locations.add(decodeFlagLocation(rc.getFlag(self_id)));
            }
            if(rememberThreat!=null){
                for(RobotInfo robot: rememberThreat){
                    locations.add(robot.getLocation());
                }
            }
            avoidLocations(locations);
            rememberThreat=null;
        }
        else if(rc.canGetFlag(self_id)&&rc.getFlag(self_id)!=0){
            locations.add(decodeFlagLocation(rc.getFlag(self_id)));
            if(rememberThreat!=null){
                for(RobotInfo robot: rememberThreat){
                    locations.add(robot.getLocation());
                }
            }
            avoidLocations(locations);
            rememberThreat=null;
        }
        else if(rememberThreat!=null){
            flee(rememberThreat);
            rememberThreat=null;
        }
        else{
            scatter();
            rememberThreat=null;
        }

    }

    // Makes the robot move randomly, with a priority for diagonals
    static void scatter () throws GameActionException {
        Direction diag = null;
        Direction straight = null;
        int overflow = 0;
        while (diag == null && overflow < 5) {
            overflow++;
            int choice = (int) (Math.random() * 4);
            switch (choice) {
                case 0:
                    if (rc.canMove(Direction.NORTHEAST))
                        diag = Direction.NORTHEAST;
                    break;
                case 1:
                    if (rc.canMove(Direction.NORTHWEST))
                        diag = Direction.NORTHWEST;
                    break;
                case 2:
                    if (rc.canMove(Direction.SOUTHEAST))
                        diag = Direction.SOUTHEAST;
                    break;
                case 3:
                    if (rc.canMove(Direction.SOUTHWEST))
                        diag = Direction.SOUTHWEST;
                    break;
            }
        }
        overflow = 0;
        while (straight == null && overflow < 5) {
            overflow++;
            int choice = (int) (Math.random() * 4);
            switch (choice) {
                case 0:
                    if (rc.canMove(Direction.NORTH))
                        straight = Direction.NORTH;
                    break;
                case 1:
                    if (rc.canMove(Direction.WEST))
                        straight = Direction.WEST;
                    break;
                case 2:
                    if (rc.canMove(Direction.EAST))
                        straight = Direction.EAST;
                    break;
                case 3:
                    if (rc.canMove(Direction.SOUTH))
                        straight = Direction.SOUTH;
                    break;
            }
        }
        if (diag == null) {
            if (straight == null) {
                return;
            } else {
                rc.move(straight);
            }
        } else if (straight == null) {
            rc.move(diag);
        } else {
            MapLocation position = rc.getLocation();
            MapLocation diagPath = position.add(diag);
            MapLocation strPath = position.add(straight);
            if (rc.sensePassability(diagPath) < (rc.sensePassability(strPath) / 1.3)) {
                rc.move(straight);
            } else {
                rc.move(diag);
            }
        }

    }

    static void avoidLocations(HashSet<MapLocation> enemies) throws GameActionException
    {
        MapLocation position= rc.getLocation();
        int yPriority = 0;
        int xPriority = 0;
        int xPos = position.x;
        int yPos = position.y;
        int radius = 64;

        for (MapLocation enemy: enemies){
            int threatX = enemy.x - xPos;
            int threatY = enemy.y - yPos;
            if (threatX < 0) {//threat is to left
                xPriority -= (radius + threatX);
            } else if (threatX > 0) {//threat is to right
                xPriority += (radius - threatX);
            }

            if (threatY < 0) {//threat is below
                yPriority -= (radius + threatY);
            } else if (threatY > 0) { //threat is above
                yPriority += (radius - threatY);
            }
        }

        moveAway(xPriority, yPriority);
    }

    // Makes the robot run away from threats
    static void flee (RobotInfo[] threat) throws GameActionException
    {
        MapLocation spot = rc.getLocation();
        int xPos = spot.x;
        int yPos = spot.y;
        int yPriority = 0;
        int xPriority = 0;
        int radius = 64;
        for (RobotInfo robot : threat) {
            MapLocation threatPos = robot.getLocation();
            int threatX = threatPos.x - xPos;
            int threatY = threatPos.y - yPos;
            if (threatX < 0) {
                xPriority -= (radius + threatX);
            } else if (threatX > 0) {
                xPriority += (radius - threatX);
            }

            if (threatY < 0) {
                yPriority -= (radius + threatY);
            } else if (threatY > 0) {
                yPriority += (radius - threatY);
            }
        }
        moveAway(xPriority, yPriority);
    }

    static void moveAway(int xPriority, int yPriority) throws GameActionException
    {
        Direction[] paths;
        if (xPriority == 0) {
            if (yPriority == 0) {
                tryMove(Direction.NORTH);
                return;
            } else if (yPriority > 0) {
                paths = new Direction[]{Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.SOUTH, Direction.EAST, Direction.WEST};
            } else {
                paths = new Direction[]{Direction.NORTHEAST, Direction.NORTHWEST, Direction.NORTH, Direction.EAST, Direction.WEST};
            }
        } else if (xPriority > 0) {
            if (yPriority == 0) {
                paths = new Direction[]{Direction.NORTHWEST, Direction.SOUTHWEST, Direction.WEST, Direction.NORTH, Direction.SOUTH};
            } else if (yPriority > 0) {
                paths = new Direction[]{Direction.SOUTHWEST, Direction.SOUTH, Direction.WEST};
            } else {
                paths = new Direction[]{Direction.NORTHWEST, Direction.NORTH, Direction.WEST};
            }
        } else {
            if (yPriority == 0) {
                paths = new Direction[]{Direction.NORTHEAST, Direction.SOUTHEAST, Direction.EAST, Direction.NORTH, Direction.SOUTH};
            } else if (yPriority > 0) {
                paths = new Direction[]{Direction.SOUTHEAST, Direction.SOUTH, Direction.EAST};
            } else {
                paths = new Direction[]{Direction.NORTHEAST, Direction.NORTH, Direction.EAST};
            }
        }
        MapLocation position = rc.getLocation();
        if (paths.length == 5) {
            double diag = 0;
            int diagTracker = -1;
            double away = 0;
            double side = 0;
            int sideTracker = -1;

            if (rc.canMove(paths[2])) {
                away = rc.sensePassability(position.add(paths[2]));
            }
            if (rc.canMove(paths[0])) {
                if (rc.canMove(paths[1])) {
                    double pass1 = rc.sensePassability(position.add(paths[0]));
                    double pass2 = rc.sensePassability(position.add(paths[1]));
                    if (pass1 > pass2) {
                        diag = pass1;
                        diagTracker = 0;
                    } else {
                        diag = pass2;
                        diagTracker = 1;
                    }
                } else {
                    diag = rc.sensePassability(position.add(paths[0]));
                    diagTracker = 0;
                }
            } else if (rc.canMove(paths[1])) {
                diag = rc.sensePassability(position.add(paths[1]));
                diagTracker = 1;
            }

            if (rc.canMove(paths[3])) {
                if (rc.canMove(paths[4])) {
                    double pass1 = rc.sensePassability(position.add(paths[3]));
                    double pass2 = rc.sensePassability(position.add(paths[4]));
                    if (pass1 > pass2) {
                        side = pass1;
                        sideTracker = 3;
                    } else {
                        side = pass2;
                        sideTracker = 4;
                    }
                } else {
                    side = rc.sensePassability(position.add(paths[3]));
                    sideTracker = 3;
                }
            } else if (rc.canMove(paths[4])) {
                side = rc.sensePassability(position.add(paths[4]));
                sideTracker = 4;
            }

            if (diag < away / 1.3 && away > 0) {
                if (away < side / 1.4) {
                    rc.move(paths[sideTracker]);
                } else {
                    rc.move(paths[2]);
                }
            } else if (diag < side / 2.0 && side > 0) {
                rc.move(paths[sideTracker]);
            } else if (diag > 0) {
                rc.move(paths[diagTracker]);
            }
        } else {
            double diag = 0;
            double straight = 0;
            int straightTracker = -1;
            if (rc.canMove(paths[0])) {
                diag = rc.sensePassability(position.add(paths[0]));
            }
            if (rc.canMove(paths[1])) {
                if (rc.canMove(paths[2])) {
                    double pass1 = rc.sensePassability(position.add(paths[1]));
                    double pass2 = rc.sensePassability(position.add(paths[2]));
                    if (pass1 > pass2) {
                        straight = pass1;
                        straightTracker = 1;
                    } else {
                        straight = pass2;
                        straightTracker = 2;
                    }
                } else {
                    straight = rc.sensePassability(position.add(paths[1]));
                    straightTracker = 1;
                }
            } else if (rc.canMove(paths[2])) {
                straight = rc.sensePassability(position.add(paths[2]));
                straightTracker = 2;
            }


            if (diag < straight / 1.3 && straightTracker > 0) {
                rc.move(paths[straightTracker]);
            } else if (diag > 0) {
                rc.move(paths[0]);
            }

        }
    }

    static void runMuckraker () throws GameActionException {

        //flags and searching mechanisms
        sendMovingFlag();

        if (!rc.isReady())
            return;

        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;

        //Expose if possible
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


        //split up the tasks based on modularity of ID (can be expanded)
        if(rc.getID() % 3 == 0){
            muckExploreEarly(Direction.NORTHEAST);
        }
        else if (rc.getID() % 3 == 1){
            muckExploreEarly(Direction.SOUTHWEST);
        }
        else{
            //consider adding something that goes toward the middle?
            tryMove(randomDirection());
        }

        if(rc.getRoundNum() > 500 && rc.canGetFlag(homeID) && rc.getID() % 2 == 0) {
            if(decodeFlagTeam(rc.getFlag(homeID)).equals(rc.getTeam().opponent())){
                pathfind(rc.getLocation().directionTo(decodeFlagLocation(rc.getFlag(homeID))));
            }
        }

        //wallBounce();
    }


    static boolean[] wallsHit = {false,false,false,false};
    static boolean hitAny = false;

    //code for moving early, takes in initial direction
    static void muckExploreEarly(Direction initial) throws GameActionException{

        //directions go north,east,south,west in that order for both arrays
        MapLocation[] directionBounds = {
                new MapLocation(rc.getLocation().x, rc.getLocation().y + 6),
                new MapLocation(rc.getLocation().x + 6, rc.getLocation().y),
                new MapLocation(rc.getLocation().x, rc.getLocation().y - 6),
                new MapLocation(rc.getLocation().x - 6, rc.getLocation().y)
        };

        //buffers in same order to test if too close to the wall
        MapLocation[] directionBuffers = {
                new MapLocation(rc.getLocation().x, rc.getLocation().y + 4),
                new MapLocation(rc.getLocation().x + 4, rc.getLocation().y),
                new MapLocation(rc.getLocation().x, rc.getLocation().y - 4),
                new MapLocation(rc.getLocation().x - 4, rc.getLocation().y)
        };

        //see if hit one of the walls
        for(int i = 0; i < directionBounds.length;i++){
            if(!rc.canDetectLocation(directionBounds[i])){
                wallsHit[i] = true;
            }
            //  System.out.println(wallsHit[i] + " " + directionBounds[i].toString());
        }

        Direction dirToMove = null;

        int j = 0;

        //if northeast, run through array in proper order starting at north, and see if walls hit
        if(initial.equals(Direction.NORTHEAST)){
            for(int i = 0; i < wallsHit.length;i++){
                if(dirToMove == null){
                    if(!wallsHit[i]){
                        dirToMove = directions[(i)*2];
                        j = i;
                    }
                }
            }
            if(wallsHit[0] == true || wallsHit[1] == true){
                hitAny = true;
                wallsHit[2] = false;
                wallsHit[3] = false;
            }
        }
        //if southwest, need to start at south and run through array and loop back
        else if(initial.equals(Direction.SOUTHWEST)){
            for(int i = 2; i < wallsHit.length;i++){
                if(dirToMove == null){
                    if(!wallsHit[i]){
                        dirToMove = directions[(i)*2];
                        j = i;
                    }
                }
            }
            for(int i = 0; i < 2;i++){
                if(dirToMove == null){
                    if(!wallsHit[i]){
                        dirToMove = directions[(i)*2];
                        j = i;
                    }
                }
            }
            if(wallsHit[2] == true || wallsHit[3] == true){
                hitAny = true;
                wallsHit[0] = false;
                wallsHit[1] = false;
            }
        }

        //if hit any of them and not all of them, check if too close to buffer
        //if so, move more inward, if not pathfind in that direction
        if(hitAny && dirToMove != null){
            //System.out.println(dirToMove.toString());
            MapLocation bufferLoc = null;
            if(j > 0){
                bufferLoc = directionBuffers[j-1];
            }
            else{
                bufferLoc = directionBuffers[3];
            }

            if(!rc.canDetectLocation(bufferLoc)){
                // System.out.println("overwrite, moving " + directions[j*2+1]);
                if(rc.canMove(directions[j*2+1])){
                    rc.move(directions[j*2+1]);
                    return;
                }
            }

            if(pathfind(dirToMove)){
                return;
            }
        }
        else{
            //  System.out.println(initial.toString());
            if(pathfind(initial)){
                return;
            }
        }

        //currently, when they are done, just move randomly, can change based on flags
        //hunt slanderers, etc.
        tryMove(randomDirection());

    }


    static Direction currentDirection = null;
    static Direction altDirection;
    static boolean stepnum = true;

    static Direction getDirection ( int dx, int dy) throws GameActionException {
        MapLocation l1 = rc.getLocation();
        return l1.directionTo(l1.translate(dx, dy));
    }

    /**
     * Takes a step in a direction until it hits a wall
     * Made for Muckrakers
     *
     * No return value, takes a step
     */
    static void wallBounce () throws GameActionException {
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
    static Direction randomDirection () {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove (Direction dir) throws GameActionException {
        // System.out.println("I am trying to move " + dir + "; " + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else
            return false;
    }
}
