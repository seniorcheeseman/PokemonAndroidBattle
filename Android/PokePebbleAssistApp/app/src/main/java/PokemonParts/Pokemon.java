package PokemonParts;

/**
 * Created by Arthur on 10/17/2015.
 */
public class Pokemon {
    private String[] mMoves;
    private int[] mTotalPP;
    private int[] mCurrentPP;
    private int mHp;
    private int mMaxhp;
    private String mName;
    private boolean mFainted;

    public Pokemon(String name, String[] moves, int[] totalPP, int maxhp) {
        mName = name;
        mMaxhp = maxhp;
        mHp = maxhp;
        mMoves = moves;
        mFainted = false;
        mTotalPP = new int[4];
        mCurrentPP = new int[4];
        for (int x = 0; x < 4; x++) {
            mTotalPP[x] = totalPP[x];
            mCurrentPP[x] = totalPP[x];
        }
    }

    public int[] getTotalPP() {
        return mTotalPP;
    }

    public int[] getCurrentPP() {
        return mCurrentPP;
    }

    public String getName() {
        return mName;
    }

    public int getHp() {
        return mHp;
    }

    public void changeCurrentPP(int move, int pp) {
        mCurrentPP[move]= pp;
    }

    public void changeTotalPP(int move,int mpp)
    {
        mTotalPP[move]= mpp;
    }


    public int getMaxhp() {
        return mMaxhp;
    }

    public void changeHp(int hp) {
        mHp = hp;
    }

    public void setFainted() {
        mFainted = true;
    }

    public boolean isFainted() {
        return mFainted;
    }
}
