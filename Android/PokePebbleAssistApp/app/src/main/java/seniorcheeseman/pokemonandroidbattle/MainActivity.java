package seniorcheeseman.pokemonandroidbattle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import PokemonParts.Party;
import PokemonParts.Pokemon;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MyActivity";
    private WebSocketClient mWebSocketClient;
    private boolean mGotPokemon;
    private Party mParty;
    private String mBattleRoom,mPVal;
    private Button mFindBattle, mForfeitButton, mAttack, mSwitch;
    private Button[] mMoves, mSwitchPokemon;
    private View.OnClickListener mFindBattleListener, mForfeitListener, mAttackListener, mSwitchListener;
    private JSONObject[] mPokemonStats;
    private TextView mCommentBar,mPokemonName,mOPokemonName;
    private final Handler handler = new Handler();
    private  Bitmap bitmap;
    private  JSONObject metadata;
    private  String jsonFilename;
    private ImageView myPokemon, opPokemon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectWebSocket();
        jsonFilename = "spritesheet.json";
        metadata = parseJSON();
        mPVal = "";
        mCommentBar = (TextView) findViewById(R.id.PokemonText);
        mPokemonName = (TextView) findViewById(R.id.mypokemonname);
        mOPokemonName = (TextView) findViewById(R.id.opponentPokemonname);
        mPokemonName.setText("");
        mOPokemonName.setText("");
        mPokemonStats = new JSONObject[6];
        mGotPokemon = false;
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.spritesheet);
        mFindBattle = (Button) findViewById(R.id.testButton);
        mForfeitButton = (Button) findViewById(R.id.forfeitButton);
        mAttack = (Button) findViewById(R.id.attack);
        mSwitch = (Button) findViewById(R.id.switchPokemon);
        mMoves = new Button[4];
        mSwitchPokemon = new Button[5];
        mSwitchPokemon[0] = (Button) findViewById(R.id.pokemon2);
        mSwitchPokemon[1] = (Button) findViewById(R.id.pokemon3);
        mSwitchPokemon[2] = (Button) findViewById(R.id.pokemon4);
        mSwitchPokemon[3] = (Button) findViewById(R.id.pokemon5);
        mSwitchPokemon[4] = (Button) findViewById(R.id.pokemon6);
        mMoves[0] = (Button) findViewById(R.id.move1);
        mMoves[1] = (Button) findViewById(R.id.move2);
        mMoves[2] = (Button) findViewById(R.id.move3);
        mMoves[3] = (Button) findViewById(R.id.move4);
        myPokemon = (ImageView) findViewById(R.id.mypokemon);
        opPokemon = (ImageView) findViewById(R.id.opponentPokemon);
        myPokemon.setImageBitmap(null);
        opPokemon.setImageBitmap(null);
        mAttackListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoveButtons();
                hideBattleButtons();
            }
        };
        mSwitchListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSwitchPokemons();
                hideBattleButtons();
            }
        };
        mForfeitListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forfeit();
                mGotPokemon = false;
                mFindBattle.setOnClickListener(mFindBattleListener);
                mFindBattle.setClickable(true);
                mFindBattle.setVisibility(View.VISIBLE);
                myPokemon.setImageBitmap(null);
                opPokemon.setImageBitmap(null);
                writePokeText("");
                mPokemonName.setText("");
                mOPokemonName.setText("");
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
                final int y = x;
                mMoves[x].setVisibility(View.VISIBLE);
                mMoves[x].setText(moves.getString(x));
                mMoves[x].setClickable(true);
                mMoves[y].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (mParty.getPokemon(0).getCurrentPP()[y] > 0) {
                                makeMove(y);
                                Toast.makeText(MainActivity.this, "You used " + moves.getString(y), Toast.LENGTH_SHORT).show();
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
            }
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
        for (int x = 1; x < 6; x++) {
            final int y = x - 1;
            mSwitchPokemon[x - 1].setVisibility(View.VISIBLE);
            mSwitchPokemon[x - 1].setText(mParty.getPokemon(x).getName());
            mSwitchPokemon[x - 1].setClickable(true);
            mSwitchPokemon[y].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mParty.getPokemon(y + 1).isFainted()) {
                        writePokeText("This pokemon has fainted.");
                    } else {
                        Toast.makeText(MainActivity.this, "You switched to " + mParty.getPokemon(y + 1).getName(), Toast.LENGTH_SHORT).show();
                        switchPokemon(y + 1);
                        hideSwitchButtons();
                        showBattleButtons();
                    }
                }
            });
        }
    }

    private void hideSwitchButtons() {
        for (int x = 0; x < 5; x++) {
            mSwitchPokemon[x].setVisibility(View.INVISIBLE);
            mSwitchPokemon[x].setClickable(false);
        }
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
        String name = mParty.getPokemon(0).getName().toLowerCase();
        name = name.replaceAll(" ","");
        Bitmap sprite = getSprite(name,"back");
        myPokemon.setImageBitmap(sprite);
        mPokemonName.setText(mParty.getPokemon(0).getName()+Integer.toString(mParty.getPokemon(0).getHp())+"/"+Integer.toString(mParty.getPokemon(0).getMaxhp()));
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
                            mPVal = temp.getString("id");
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
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    showBattleButtons();
                                    String name = mParty.getPokemon(0).getName()+Integer.toString(mParty.getPokemon(0).getHp())+"/"+Integer.toString(mParty.getPokemon(0).getMaxhp());
                                    mPokemonName.setText(name);
                                    String temp = mParty.getPokemon(0).getName();
                                    temp = temp.toLowerCase();
                                    temp = temp.replaceAll(" ","");
                                    Bitmap sprite = getSprite(temp, "back");
                                    myPokemon.setImageBitmap(sprite);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
                            Toast.makeText(MainActivity.this, "Your pokemon has fainted. You must switch pokemon", Toast.LENGTH_SHORT).show();
                        } else {
                            String[] part = message.split("\\|");
                            String hps = part[2];
                            String[] healths = hps.split("/");
                            mParty.getPokemon(0).changeHp(Integer.parseInt(healths[0]));
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                String temp = mParty.getPokemon(0).getName()+Integer.toString(mParty.getPokemon(0).getHp())+"/"+Integer.toString(mParty.getPokemon(0).getMaxhp());
                                mPokemonName.setText(temp);
                                String name = mParty.getPokemon(0).getName();
                                name = name.toLowerCase();
                                name = name.replaceAll(" ","");
                                Bitmap sprite = getSprite(name, "back");
                                myPokemon.setImageBitmap(sprite);
                            }
                        });
                        Log.d("Hploss", Integer.toString(mParty.getPokemon(0).getHp()));
                    }
                }
                if(message.contains("switch|"))
                {
                    String tt = (mPVal.equals("p1"))?"p2":"p1";
                    String temp =  message.substring(message.indexOf("switch|"+tt));
                    final String[] part = temp.split("\\|");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            String op = part[1].substring(4) + part[3];
                            mOPokemonName.setText(op);
                            String input = part[1].substring(4);
                            input = input.replaceAll(" ","");
                            input = input.toLowerCase();
                            Bitmap sprite = getSprite(input, "front");
                            opPokemon.setImageBitmap(sprite);
                        }
                    });
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



    private  JSONObject parseJSON() {
        try {
            InputStream is = getAssets().open(jsonFilename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            return new JSONObject(json).getJSONObject("frames");
        }catch(Exception e)
        {
            return null;
        }
    }

    public Bitmap getSprite(String pokemon, String side) {
        if (!side.equals("front") && !side.equals("back")) {
            return null;
        }
        try {
            JSONObject pokemonObj = metadata.getJSONObject(pokemon + "-" + side);
            JSONObject dim = pokemonObj.getJSONObject("frame");
            int left = 2*dim.getInt("x");
            int top = 2*dim.getInt("y");
            int width = 2*dim.getInt("w");
            int height = 2*dim.getInt("h");
            return Bitmap.createBitmap(bitmap, left, top, width, height);
        } catch (JSONException e) {
            return null;
        }
    }
}
