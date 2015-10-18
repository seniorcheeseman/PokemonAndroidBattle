package seniorcheeseman.pokemonandroidbattle;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

import PokemonParts.Party;
import PokemonParts.Pokemon;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MyActivity";
    private WebSocketClient mWebSocketClient;
    private boolean mGotPokemon;
    private Party mParty;
    private String mBattleRoom;
    private Button mFindBattle, mForfeitButton, mAttack, mSwitch;
    private Button[] mMoves,mSwitchPokemon;
    private View.OnClickListener mFindBattleListener, mForfeitListener, mAttackListener, mSwitchListener;
    private JSONObject[] mPokemonStats;
    private TextView mCommentBar;
    private final Handler handler = new Handler();
    private boolean mWaitForTurn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectWebSocket();
        mCommentBar = (TextView) findViewById(R.id.PokemonText);
        mPokemonStats = new JSONObject[6];
        mGotPokemon = false;
        mFindBattle = (Button) findViewById(R.id.testButton);
        mForfeitButton = (Button) findViewById(R.id.forfeitButton);
        mAttack = (Button) findViewById(R.id.attack);
        mSwitch = (Button) findViewById(R.id.switchPokemon);
        mMoves = new Button[4];
        mSwitchPokemon = new Button[5];
        mMoves[0] = (Button) findViewById(R.id.move1);
        mMoves[1] = (Button) findViewById(R.id.move2);
        mMoves[2] = (Button) findViewById(R.id.move3);
        mMoves[3] = (Button) findViewById(R.id.move4);
        mWaitForTurn = false;
        mAttackListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mWaitForTurn)
                {

                }
                else {
                    mWaitForTurn = false;
                    showMoveButtons();
                    hideBattleButtons();
                }
            }
        };
        mSwitchListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mWaitForTurn)
                {

                }
                else {
                    showSwitchPokemons();
                    hideBattleButtons();
                }
            }
        };
        mForfeitListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forfeit();
                mFindBattle.setOnClickListener(mFindBattleListener);
                mFindBattle.setClickable(true);
                mFindBattle.setVisibility(View.VISIBLE);
                hideBattleButtons();
            }
        };
        mFindBattleListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findRandomBattle();
                mFindBattle.setOnClickListener(null);
                mFindBattle.setClickable(false);
                mFindBattle.setVisibility(View.INVISIBLE);
                writePokeText("Searching for a friendly competitor.");
            }
        };
        mFindBattle.setOnClickListener(mFindBattleListener);
    }

    private void writePokeText(String message) {
        mCommentBar.setText(message);
    }

    private void showMoveButtons() {
        try {
            JSONObject current = mPokemonStats[0];
            final JSONArray moves = current.getJSONArray("moves");
            for (int x = 0; x < 4; x++) {
                mMoves[x].setVisibility(View.VISIBLE);
                mMoves[x].setText(moves.getString(x));
                mMoves[x].setClickable(true);
            }
            mMoves[0].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (mParty.getPokemon(0).getCurrentPP()[0] >= 0) {
                            makeMove(0);
                            Toast.makeText(MainActivity.this, "You used " + moves.getString(0), Toast.LENGTH_SHORT).show();
                            mWaitForTurn = true;
                            hideMoveButtons();
                            showBattleButtons();
                        } else {
                            writePokeText("Your pokemon is too tired to do that.");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            mMoves[1].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (mParty.getPokemon(0).getCurrentPP()[1] >= 0) {
                            makeMove(1);
                            Toast.makeText(MainActivity.this, "You used " + moves.getString(1), Toast.LENGTH_SHORT).show();;
                            hideMoveButtons();mWaitForTurn = true;
                            showBattleButtons();
                        } else {
                            writePokeText("Your pokemon is too tired to do that.");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            mMoves[2].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (mParty.getPokemon(0).getCurrentPP()[2] >= 0) {
                            makeMove(2);
                            Toast.makeText(MainActivity.this, "You used " + moves.getString(2), Toast.LENGTH_SHORT).show();;
                            hideMoveButtons();mWaitForTurn = true;
                            showBattleButtons();
                        } else {
                            writePokeText("Your pokemon is too tired to do that.");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            mMoves[3].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (mParty.getPokemon(0).getCurrentPP()[2] >= 0) {
                            makeMove(3);
                            Toast.makeText(MainActivity.this, "You used " + moves.getString(3), Toast.LENGTH_SHORT).show();;
                            hideMoveButtons();mWaitForTurn = true;
                            showBattleButtons();
                        } else {
                            writePokeText("Your pokemon is too tired to do that.");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void hideMoveButtons() {
        for (int x = 0; x < 4; x++) {
            mMoves[x].setVisibility(View.INVISIBLE);
            mMoves[x].setClickable(false);
            mMoves[x].setOnClickListener(null);
        }
    }

    private void showSwitchPokemons() {

    }

    private void showBattleButtons() {
        writePokeText("Time to make a decision.");
        mForfeitButton.setOnClickListener(mForfeitListener);
        mForfeitButton.setVisibility(View.VISIBLE);
        mForfeitButton.setClickable(true);
        mAttack.setOnClickListener(mAttackListener);
        mAttack.setVisibility(View.VISIBLE);
        mAttack.setClickable(true);
        mSwitch.setOnClickListener(mSwitchListener);
        mSwitch.setVisibility(View.VISIBLE);
        mSwitch.setClickable(true);
    }

    private void hideBattleButtons() {
        mForfeitButton.setOnClickListener(null);
        mForfeitButton.setVisibility(View.INVISIBLE);
        mForfeitButton.setClickable(false);
        mAttack.setOnClickListener(null);
        mAttack.setVisibility(View.INVISIBLE);
        mAttack.setClickable(false);
        mSwitch.setOnClickListener(null);
        mSwitch.setVisibility(View.INVISIBLE);
        mSwitch.setClickable(false);
    }

    private void forfeit() {
        String giveUp = mBattleRoom + "|/forfeit";
        Toast.makeText(this, "GG Try again", Toast.LENGTH_SHORT).show();
        sendMessage(giveUp);
        mForfeitButton.setOnClickListener(null);//todo make it invisible
        mFindBattle.setOnClickListener(mFindBattleListener);
    }


    private void findRandomBattle() {
        sendMessage("|/cancelsearch");
        sendMessage("|/search randombattle");
    }

    private void makeMove(int move) {
        String in = Integer.toString(move + 1);
        sendMessage(mBattleRoom + "|/move " + in);
    }

    private void switchPokemon(int pos) {
        String in = Integer.toString(pos + 1);
        mParty.switchPokemon(0, pos);
        JSONObject temp = mPokemonStats[0];
        mPokemonStats[0] = mPokemonStats[pos];
        mPokemonStats[pos] = temp;
        sendMessage(mBattleRoom + "|/switch " + in);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebSocketClient.close();
    }

    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://159.203.89.223:8000/showdown/websocket");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {

            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                if (s.contains("request")) {
                    if (!mGotPokemon) {
                        String[] parts = message.split("request");
                        JSONObject part;
                        try {
                            part = getParty(parts[1].substring(1));//hard coded
                            mGotPokemon = true;
                            JSONObject temp = part.getJSONObject("side");
                            JSONArray pokes = temp.getJSONArray("pokemon");
                            Pokemon[] pokemons = new Pokemon[6];
                            for (int x = 0; x < pokes.length(); x++) {
                                int[] pp = {12, 12, 12, 12};
                                String[] moves = new String[4];
                                JSONObject poke = (JSONObject) pokes.get(x);
                                mPokemonStats[x] = poke;
                                JSONArray pokeMoves = (JSONArray) poke.get("moves");
                                for (int y = 0; y < 4; y++) {
                                    moves[y] = (String) pokeMoves.get(y);
                                }
                                String name = ((JSONObject) (pokes.get(x))).getString("ident").split(":")[1];
                                int hp = Integer.parseInt(((JSONObject) (pokes.get(x))).getString("condition").split("/")[1]);
                                pokemons[x] = new Pokemon(name, moves, pp, hp);
                            }
                            mParty = new Party(pokemons);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                showBattleButtons();
                            }
                        });

                    } else if (message.contains("active")) {
                        String[] parts = message.split("request");
                        JSONObject part;
                        try {
                            part = getParty(parts[1].substring(1));
                            JSONArray temp = part.getJSONArray("active");
                            JSONObject first = (JSONObject) temp.get(0);
                            temp = first.getJSONArray("moves");
                            for (int x = 0; x < first.length(); x++) {
                                JSONObject moves = (JSONObject) temp.get(x);
                                if (!moves.getBoolean("disabled"))
                                    mParty.getPokemon(0).changeCurrentPP(x, moves.getInt("pp"));
                                else
                                    mParty.getPokemon(0).changeCurrentPP(x, 0);
                                mParty.getPokemon(0).changeTotalPP(x, moves.getInt("maxpp"));
                                Log.d("PPChanges", Integer.toString(moves.getInt("pp")));
                                Log.d("PPChanges", Integer.toString(moves.getInt("maxpp")));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (message.contains("battle-randombattle") && mBattleRoom == null) {
                    String[] notBattleRoom = message.split("\\|");
                    mBattleRoom = notBattleRoom[0].substring(1);
                    mBattleRoom = mBattleRoom.replaceAll("\n", "");
                    Log.d("BattleRoom", mBattleRoom);
                } else if ((message.contains("-damage") || message.contains("-heal")) && mParty != null) {
                    String pokemonName = mParty.getPokemon(0).getName();
                    if (message.contains(pokemonName)) {
                        if (message.contains("fnt")) {
                            mParty.getPokemon(0).setFainted();
                            mParty.getPokemon(0).changeHp(0);
                        } else {
                            String[] part = message.split("\\|");
                            String hps = part[2];
                            String[] healths = hps.split("/");
                            mParty.getPokemon(0).changeHp(Integer.parseInt(healths[0]));
                        }
                        Log.d("Hploss", Integer.toString(mParty.getPokemon(0).getHp()));
                    }
                    mWaitForTurn = false;
                }
                Log.d(TAG, message);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

    public void sendMessage(String message) {
        mWebSocketClient.send(message);
        Log.d("WebsocketMessages", message);
//        Toast.makeText(this, message + ": has been sent", Toast.LENGTH_LONG).show();
    }

    /**
     * Parses the websocket input to get jsonarray of the pokemon
     *
     * @param input
     * @return JSONObject of pokemon
     */
    public JSONObject getParty(String input) throws JSONException {
        JSONObject party = new JSONObject(input);
        return party;
    }

}
