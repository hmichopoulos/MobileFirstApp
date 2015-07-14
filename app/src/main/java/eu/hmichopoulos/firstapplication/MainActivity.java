package eu.hmichopoulos.firstapplication;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import static javax.crypto.Cipher.*;

public class MainActivity extends ActionBarActivity {

    public void scanQR(View v) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        String str = scanResult.getContents();
        str = "{ \"sessionId\": 0,  \"key\": \"XxXx-EdEd-Eq\" }";

        try {
            ((TextView)findViewById(R.id.textView)).setText(str);
            JSONObject response = new JSONObject(str);
            int sessionId = (Integer) response.get("sessionId");
            final String key = (String) response.get("key");

            final String url = "http://78.47.49.90:8080/sessionInfo/" + sessionId;
            final TextView saltTextView = (TextView)findViewById(R.id.saltTextView);

            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            String salt = null;
                            try {
                                salt = (String) response.get("salt");

                                String plainText = salt + "My Plain Text";

                                DESKeySpec keySpec = new DESKeySpec(key.getBytes("UTF8"));
                                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
                                SecretKey key = keyFactory.generateSecret(keySpec);

                                Cipher cipher = getInstance("DES");
                                cipher.init(ENCRYPT_MODE, key);
                                final String encrypedValue = Base64.encodeToString(cipher.doFinal(plainText.getBytes()), Base64.DEFAULT);

                                ((TextView)findViewById(R.id.textView)).setText(encrypedValue);

                                response.put("cipherText", encrypedValue);

                                JsonObjectRequest updateRequest = new JsonObjectRequest(
                                        Request.Method.POST,
                                        url,
                                        response,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                ((TextView)findViewById(R.id.textView)).setText(response.toString());
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                ((TextView)findViewById(R.id.textView)).setText(error.getMessage());
                                            }
                                        }
                                );

                                MySingleton.getInstance(MainActivity.this).addToRequestQueue(updateRequest);

                            } catch (JSONException e) {
                                ((TextView)findViewById(R.id.textView)).setText(e.getMessage());
                                e.printStackTrace();
                            } catch (NoSuchPaddingException e) {
                                ((TextView)findViewById(R.id.textView)).setText(e.getMessage());
                                e.printStackTrace();
                            } catch (BadPaddingException e) {
                                ((TextView)findViewById(R.id.textView)).setText(e.getMessage());
                                e.printStackTrace();
                            } catch (NoSuchAlgorithmException e) {
                                ((TextView)findViewById(R.id.textView)).setText(e.getMessage());
                                e.printStackTrace();
                            } catch (IllegalBlockSizeException e) {
                                ((TextView)findViewById(R.id.textView)).setText(e.getMessage());
                                e.printStackTrace();
                            } catch (UnsupportedEncodingException e) {
                                ((TextView)findViewById(R.id.textView)).setText(e.getMessage());
                                e.printStackTrace();
                            } catch (InvalidKeyException e) {
                                ((TextView)findViewById(R.id.textView)).setText(e.getMessage());
                                e.printStackTrace();
                            } catch (InvalidKeySpecException e) {
                                ((TextView)findViewById(R.id.textView)).setText(e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            ((TextView)findViewById(R.id.textView)).setText(error.getMessage());
                            saltTextView.setText(error.getMessage());
                        }
                    });

            MySingleton.getInstance(this).addToRequestQueue(jsObjRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //((TextView) findViewById(R.id.textView)).setText(str);

        //split parts, Take sessionId / secret key

    }

    public void encrypt(View v) {
        try {
            String cryptoPass = "MyPassword";
            DESKeySpec keySpec = new DESKeySpec(cryptoPass.getBytes("UTF8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            byte[] clearText = ((EditText)findViewById(R.id.editText)).getText().toString().getBytes();

            Cipher cipher = getInstance("DES");
            cipher.init(ENCRYPT_MODE, key);
            String encrypedValue = Base64.encodeToString(cipher.doFinal(clearText), Base64.DEFAULT);

            ((TextView)findViewById(R.id.textView2)).setText(encrypedValue);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void contact(View v) {
        String url = "http://172.17.7.16:8080/sessionInfo";
        final TextView saltTextView = (TextView)findViewById(R.id.saltTextView);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String salt = null;
                        try {
                            salt = (String) response.get("salt");
                            saltTextView.setText(salt);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        saltTextView.setText(error.getMessage());
                    }
                });

        MySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
