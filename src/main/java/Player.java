import java.util.ArrayList;
import java.util.Scanner;

/**
 * Grab Snaffles and try to throw them through the opponent's goal!
 * Move towards a Snaffle and use your team id to determine where you need to throw it.
 *
 *
 * Standard input ->   [teamID, myScore, myMagic, opponentScore, opponentMagic, numOfEntities, entity1, [x, y, vx, vy, state] , entity2, [x, y, vx, vy, state], ..., entityN, [x, y, vx, vy, state]]
 **/
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int myTeamId = in.nextInt(); // if 0 you need to score on the right of the map, if 1 you need to score on the left




        // game loop
        while (true) {
            ArrayList<Wizard> myWizards = new ArrayList<>();
            ArrayList<Wizard> enemyWizards = new ArrayList<>();
            ArrayList<Snaffle> snaffles = new ArrayList<>();

            int myScore = in.nextInt();
            int myMagic = in.nextInt();
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

                if (entityType.equals("WIZARD")){
                    myWizards.add(new Wizard(entityId, entityType, x, y, vx, vy, state, myTeamId));
                    System.err.println("myWizard added");
                } else if (entityType.equals("OPPONENT_WIZARD")){
                    enemyWizards.add(new Wizard(entityId, entityType, x, y, vx, vy, state, myTeamId ^ 1));
                    System.err.println("enemyWizard added");
                } else if (entityType.equals("SNAFFLE")) {
                    snaffles.add(new Snaffle(entityId, entityType, x, y, vx, vy));
                    System.err.println("Snaffle added");
                }

            }
            for (int i = 0; i < 2; i++) {

                // Write an action using System.out.println()
                // To debug: System.err.println("Debug messages...");

                // Edit this line to indicate the action for each wizard (0 ≤ thrust ≤ 150, 0 ≤ power ≤ 500)
                // i.e.: "MOVE x y thrust" or "THROW x y power"

                myWizards.get(i).action(myWizards, enemyWizards, snaffles);


            }
        }
    }


}

class Entity {
    int entityId;
    String entityType;
    int x;
    int y;
    int vx;
    int vy;
    boolean isTargeted;
    Entity targetedBy;

    public Entity(int entityId, String entityType, int x, int y, int vx, int vy) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean getIsTargeted(){ return isTargeted; }

    /** Sets what entity is targeting this entity.
     *
     * @param e The entity targeting this entity. null to clear the targetedBy entity.
     */
    public void setTargetedBy(Entity e){
        if (null != e) {
            isTargeted = true;
            targetedBy = e;
        } else {
            isTargeted = false;
        }
    }

    /**
     * Compares two entities, and returns their distance.
     * @param a
     * @param b
     * @return The squared distance between two entities.
     */
    public double distanceBetween(Entity a, Entity b){
        int x1 = a.getX();
        int x2 = b.getX();
        int y1 = a.getY();
        int y2 = b.getY();

        return Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2);
    }

    /**
     * This method calculates which untargeted snaffle is closest to this object.
     * @param snaffles The list of snaffles on the field.
     * @return The best candidate for targeting. Null if no best candidate.
     */
    public Snaffle getNearestUntargetedSnaffle(ArrayList<Snaffle> snaffles){
        Snaffle best = null;
        for (Snaffle s : snaffles){
            // Make the first targetable snaffle, the current best.
            if (!s.getIsTargeted()) {
                best = s;
                break;
            }
        }
        // If all are targeted, return null.
        if (best == null)
            return null;

        for (int i = 0; i < snaffles.size(); i++){
            Snaffle currentSnaffle = snaffles.get(i);
            // If the snaffle is not already targetted.
            if (!currentSnaffle.getIsTargeted()){
                    if (distanceBetween(currentSnaffle, this) <= distanceBetween(best, this))
                        best = currentSnaffle;

            }
        }

        return best;
    }


}

class Wizard extends Entity {
    private int state;
    private int teamID; // (X=0, Y=3750) for team 0 and (X=16000, Y=3750) for team 1.
    private Entity currentTarget;


    public Wizard(int entityId, String entityType, int x, int y, int vx, int vy, int state, int teamID) {
        super(entityId, entityType, x, y, vx, vy);
        this.state = state;
        this.teamID = teamID;
    }

    /**
     * Target the passed in entity.
     * @param target The entity to be targeted.
     */
    public void target(Entity target){
        currentTarget = target;
        target.setTargetedBy(this);
    }

    public void action(ArrayList<Wizard> myWizards, ArrayList<Wizard> enemyWizards, ArrayList<Snaffle> snaffles){
        if (state == 0) {
            Snaffle target = getNearestUntargetedSnaffle(snaffles);
            if (target != null) {
                int x = target.getX();
                int y = target.getY();
                System.out.println("MOVE " + x + " " + y + " " + 150);
            }
        } else {
            int x = teamID == 0 ? 16000 : 0;
            int y = 3750;
            System.out.println("THROW " + x + " " + y + " " + 500);
        }
    }
}

class Snaffle extends Entity {


    public Snaffle(int entityId, java.lang.String entityType, int x, int y, int vx, int vy) {
        super(entityId, entityType, x, y, vx, vy);
    }


}