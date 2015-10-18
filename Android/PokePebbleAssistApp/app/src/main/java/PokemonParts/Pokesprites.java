package PokemonParts;

import java.io.InputStream;
import org.json.JSONObject;
import org.json.JSONException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

/**
 * Created by Andrew on 10/18/2015.
 */

public class Pokesprites {
    private static String jsonFilename = "spritesheet.json";

    private static JSONObject parseJSON() {
        InputStream is = getAssets().open(jsonFilename);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        String json = new String(buffer, "UTF-8");
        return new JSONObject(json);
    }

    private static JSONObject metadata = parseJSON();

    private static Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.spritesheet);

    public static Bitmap getBitmap() {
        return bitmap;
    }

    public static Rect getSpriteRect(String pokemon, String side) {
        if (!side.equals("front") && !side.equals("back")) {
            return null;
        }
        try {
            JSONObject pokemonObj = metadata.getJSONObject(pokemon + "-" + side);
            JSONObject dim = pokemonObj.getJSONObject("frame");
            int left = dim.getInt("x");
            int top = dim.getInt("y");
            int right = left + dim.getInt("w");
            int bottom = top + dim.getInt("h");
            return new Rect(left, top, right, bottom);
        } catch (JSONException e) {
            return null;
        }
    }
}
