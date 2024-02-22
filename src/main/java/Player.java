import java.util.ArrayList;
import java.util.Scanner;
import java.time.Instant;

/**
 * Grab Snaffles and try to throw them through the opponent's goal!
 * Move towards a Snaffle and use your team id to determine where you need to throw it.
 * <p>
 * <p>
 * Standard input ->   [teamID, myScore, myMagic, opponentScore, opponentMagic, numOfEntities, entity1, [x, y, vx, vy, state] , entity2, [x, y, vx, vy, state], ..., entityN, [x, y, vx, vy, state]]
 **/
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int myTeamId = in.nextInt(); // if 0 you need to score on the right of the map, if 1 you need to score on the left


        // game loop
        while (true) {
            /* Timestamp */
            Instant start = Instant.now();
            long startMS = start.toEpochMilli();

            ArrayList<Wizard> myWizards = new ArrayList<>();
            ArrayList<Wizard> enemyWizards = new ArrayList<>();
            ArrayList<Snaffle> snaffles = new ArrayList<>();
            ArrayList<Bludger> bludgers = new ArrayList<Bludger>();

            int myScore = in.nextInt();
            Integer myMagic = in.nextInt();
            int opponentScore = in.nextInt();
            int opponentMagic = in.nextInt();
            int entities = in.nextInt(); // number of entities still in game
            for (int i = 0; i < entities; i++) {
                int entityId = in.nextInt(); // entity identifier
                String entityType = in.next(); // "WIZARD", "OPPONENT_WIZARD" or "SNAFFLE" (or "BLUDGER" after first league)
                int x = in.nextInt(); // position
                int y = in.nextInt(); // position
                int vx = in.nextInt(); // velocity
                int vy = in.nextInt(); // velocity
                int state = in.nextInt(); // 1 if the wizard is holding a Snaffle, 0 otherwise

                if (entityType.equals("WIZARD")) {
                    myWizards.add(new Wizard(entityId, entityType, x, y, vx, vy, state, myTeamId, myMagic));
                    System.err.println("myWizard added with velocity x: " + vx + " y: " + vy);
                } else if (entityType.equals("OPPONENT_WIZARD")) {
                    enemyWizards.add(new Wizard(entityId, entityType, x, y, vx, vy, state, myTeamId ^ 1, opponentMagic));
                    System.err.println("enemyWizard added");
                } else if (entityType.equals("SNAFFLE")) {
                    snaffles.add(new Snaffle(entityId, entityType, x, y, vx, vy));
                    System.err.println("Snaffle added");
                } else if (entityType.equals("BLUDGER")) {
                    bludgers.add(new Bludger(entityId, entityType, x, y, vx, vy));
                    System.err.println("Bludger added");
                }

            }
            for (int i = 0; i < 2; i++) {

                // Write an action using System.out.println()
                // To debug: System.err.println("Debug messages...");

                // Edit this line to indicate the action for each wizard (0 ≤ thrust ≤ 150, 0 ≤ power ≤ 500)
                // i.e.: "MOVE x y thrust" or "THROW x y power"

                myWizards.get(i).action(myWizards, enemyWizards, snaffles, bludgers);


            }

            /* Print the time passed this turn */
            Instant end = Instant.now();
            long endMS = end.toEpochMilli();
            System.err.println("ms elapsed: " + (endMS - startMS));
        }
    }


}

class Entity {
    private int entityId;
    private String entityType;
    private int x;
    private int y;
    private int vx;
    private int vy;
    private int futureX;
    private int futureY;
    private boolean isTargeted;


    public Entity(int entityId, String entityType, int x, int y, int vx, int vy) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.futureX = x + vx;
        this.futureY = y + vy;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getVx() {
        return vx;
    }

    public int getVy() {
        return vy;
    }

    public int getFutureX() {
        return futureX;
    }

    public int getFutureY() {
        return futureY;
    }

    public boolean getIsTargeted() {
        return isTargeted;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setTargeted(Boolean b) {
        isTargeted = b;
    }

    /**
     * Compares two entities, and returns their distance.
     *
     * @param a
     * @param b
     * @return The approximate distance between two entities.
     */
    public int distanceBetweenTwoEntities(Entity a, Entity b) {
        int x1 = a.getX();
        int x2 = b.getX();
        int y1 = a.getY();
        int y2 = b.getY();

        return (int) Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
    }

    /**
     * This method calculates which untargeted snaffle is closest to this object. Mainly used for wizard targeting.
     *
     * @param snaffles The list of snaffles on the field.
     * @return The best candidate for targeting. Null if no best candidate.
     */
    public Snaffle getNearestUntargetedSnaffle(ArrayList<Snaffle> snaffles) {
        if (snaffles.size() == 1)
            return snaffles.get(0);
        Snaffle best = null;
        for (Snaffle s : snaffles) {
            // Make the first targetable snaffle, the current best.
            if (!s.getIsTargeted()) {
                best = s;
                break;
            }
        }
        // If all are targeted, return null.
        if (best == null)
            return null;

        for (int i = 0; i < snaffles.size(); i++) {
            Snaffle currentSnaffle = snaffles.get(i);
            // If the snaffle is not already targeted.
            if (!currentSnaffle.getIsTargeted()) {
                if (distanceBetweenTwoEntities(currentSnaffle, this) <= distanceBetweenTwoEntities(best, this))
                    best = currentSnaffle;

            }
        }

        return best;
    }


    @Override
    public String toString() {
        return entityType + " " + entityId + " at: (" + x + "," + y + ")";
    }


}

class Wizard extends Entity {
    private int state;
    private int teamID; // (X=0, Y=3750) for team 0 and (X=16000, Y=3750) for team 1.
    private int magic;
    private Entity currentTarget;


    public Wizard(int entityId, String entityType, int x, int y, int vx, int vy, int state, int teamID, int magic) {
        super(entityId, entityType, x, y, vx, vy);
        this.state = state;
        this.teamID = teamID;
        this.magic = magic;
    }

    /**
     * Target the passed in entity.
     *
     * @param target The entity to be targeted.
     */
    public void setTarget(Entity target) {
        currentTarget = target;
        if (target != null)
            target.setTargeted(true);
    }

    public void action(ArrayList<Wizard> myWizards, ArrayList<Wizard> enemyWizards, ArrayList<Snaffle> snaffles, ArrayList<Bludger> bludgers) {
        System.err.println("internal magic: " + this.magic);
        int goalX = teamID == 0 ? 16000 : 0;
        int goalY = 2500;
        /* find snaffle logic */
        if (state == 0) {
            Entity target;
            if (currentTarget == null) {
                target = getNearestUntargetedSnaffle(snaffles);
            } else {
                target = currentTarget;
            }
            if (target != null) {
                /* If there is a snaffle between the wizard and the wizard's goal, cast ACCIO on it. */
                int distanceBetweenWizardAndTarget = distanceBetweenTwoEntities(this, target);
                System.err.println("distanceBetweenWizardAndTarget: " + distanceBetweenWizardAndTarget);
                if (this.magic >= 15 && (distanceBetweenWizardAndTarget >= 1500 && distanceBetweenWizardAndTarget <= 6000) && ((teamID == 0 && target.getFutureX() < this.getFutureX()) || (teamID == 1 && target.getFutureX() > this.getFutureX()))) {
                    this.magic -= 15;
                    System.out.println("ACCIO " + target.getEntityId());
                } else {
                    setTarget(target);
                    System.err.println(this + "targets " + target);
                    System.out.println("MOVE " + target.getFutureX() + " " + target.getFutureY() + " " + 150);
                }
            } else {
                setTarget(null);
            }
        } else {
            /* holding snaffle logic */
            setTarget(null);
            int[] bestShot = findBestShot(goalX, goalY, enemyWizards, snaffles, bludgers);
            makeShot(bestShot[0], bestShot[1]);
        }
    }

    /**
     * Finds the best shot for the thrower. This post was extremely helpful in creating the logic for this method: https://stackoverflow.com/a/17693146/23110483
     *
     * @param goalX
     * @param goalY
     * @param enemyWizards
     * @param snaffles
     * @param bludgers
     * @return
     */
    public int[] findBestShot(int goalX, int goalY, ArrayList<Wizard> enemyWizards, ArrayList<Snaffle> snaffles, ArrayList<Bludger> bludgers) {
        int throwerX = this.getX();
        int throwerY = this.getY();
        int tolerance = 10;
        if (goalY > 5500)
            return new int[]{0, 0};

        for (Wizard enemy : enemyWizards) {
            int enemyX = enemy.getFutureX();
            int enemyY = enemy.getFutureY();
            int wizardRadius = 150;
            /* Only bother checking distances if it's possible that the enemy is between the thrower and the goal */
            if ((teamID == 0 && enemyX <= goalX && enemyX > throwerX) || (teamID == 1 && enemyX < throwerX && enemyX >= goalX)) {
                int distanceEnemyAndGoal = getDistanceBetweenTwoPoints(enemyX, enemyY, goalX, goalY);
                int distanceThrowerAndEnemy = getDistanceBetweenTwoPoints(throwerX, throwerY, enemyX, enemyY);
                int distanceThrowerAndGoal = getDistanceBetweenTwoPoints(throwerX, throwerY, goalX, goalY);

                /* If the distance of the thrower to the enemy plus the enemy to the goal is equal to the distance of the thrower to the goal +- 10 units, then there is something in the way. */
                if (distanceEnemyAndGoal + distanceThrowerAndEnemy - distanceThrowerAndGoal < tolerance)
                    return findBestShot(goalX, goalY + wizardRadius, enemyWizards, snaffles, bludgers);
            }
        }

        for (Bludger enemy : bludgers) {
            int enemyX = enemy.getFutureX();
            int enemyY = enemy.getFutureY();
            int bludgerRadius = 100;
            /* Only bother checking distances if it's possible that the enemy is between the thrower and the goal */
            if ((teamID == 0 && enemyX <= goalX && enemyX > throwerX || teamID == 1 && enemyX < throwerX && enemyX >= goalX)) {
                int distanceEnemyAndGoal = getDistanceBetweenTwoPoints(enemyX, enemyY, goalX, goalY);
                int distanceThrowerAndEnemy = getDistanceBetweenTwoPoints(throwerX, throwerY, enemyX, enemyY);
                int distanceThrowerAndGoal = getDistanceBetweenTwoPoints(throwerX, throwerY, goalX, goalY);

                /* If the distance of the thrower to the enemy plus the enemy to the goal is equal to the distance of the thrower to the goal +- 10 units, then there is something in the way. */
                if (distanceEnemyAndGoal + distanceThrowerAndEnemy - distanceThrowerAndGoal < tolerance)
                    return findBestShot(goalX, goalY + bludgerRadius, enemyWizards, snaffles, bludgers);
            }
        }

        for (Snaffle enemy : snaffles) {
            int enemyX = enemy.getFutureX();
            int enemyY = enemy.getFutureY();
            int snaffleRadius = 75;
            /* Only bother checking distances if it's possible that the enemy is between the thrower and the goal */
            if ((teamID == 0 && enemyX <= goalX && enemyX > throwerX || teamID == 1 && enemyX < throwerX && enemyX >= goalX)) {
                int distanceEnemyAndGoal = getDistanceBetweenTwoPoints(enemyX, enemyY, goalX, goalY);
                int distanceThrowerAndEnemy = getDistanceBetweenTwoPoints(throwerX, throwerY, enemyX, enemyY);
                int distanceThrowerAndGoal = getDistanceBetweenTwoPoints(throwerX, throwerY, goalX, goalY);

                /* If the distance of the thrower to the enemy plus the enemy to the goal is equal to the distance of the thrower to the goal +- 10 units, then there is something in the way. */
                if (distanceEnemyAndGoal + distanceThrowerAndEnemy - distanceThrowerAndGoal < tolerance)
                    return findBestShot(goalX, goalY + snaffleRadius, enemyWizards, snaffles, bludgers);
            }
        }

        return new int[]{goalX, goalY};
    }

    public void makeShot(int x, int y) {
        System.out.println("THROW " + x + " " + y + " " + 500);
    }

    /**
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return The approximate distance between two points.
     */
    public int getDistanceBetweenTwoPoints(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
}

class Snaffle extends Entity {

    public Snaffle(int entityId, java.lang.String entityType, int x, int y, int vx, int vy) {
        super(entityId, entityType, x, y, vx, vy);
    }

}

class Bludger extends Entity {

    public Bludger(int entityId, java.lang.String entityType, int x, int y, int vx, int vy) {
        super(entityId, entityType, x, y, vx, vy);
    }

}