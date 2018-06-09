package io.move.move;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

public class LoginActivity extends AppCompatActivity {

    String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final String countryCode = getResources().getString(R.string.country_code) + " ";
        final EditText phoneNumberEditText = findViewById(R.id.phone_number_edit_text);
        phoneNumberEditText.setText(countryCode);
        Selection.setSelection(phoneNumberEditText.getText(), phoneNumberEditText.getText().length());

        phoneNumberEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().startsWith(countryCode)) {
                    phoneNumberEditText.setText(countryCode);
                    Selection.setSelection(phoneNumberEditText.getText(), phoneNumberEditText.getText().length());
                }

                char space = ' ';
                // Remove spacing char
                if (s.length() > 0 && (s.length() % 10) == 0) {
                    final char c = s.charAt(s.length() - 1);
                    if (space == c) {
                        s.delete(s.length() - 1, s.length());
                    }
                }
                // Insert char where needed.
                if (s.length() > 0 && (s.length() % 10) == 0) {
                    char c = s.charAt(s.length() - 1);
                    // Only if its a digit where there should be a space we insert a space
                    if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(space)).length <= 8) {
                        s.insert(s.length() - 1, String.valueOf(space));
                    }
                }
            }
        });

        findViewById(R.id.login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String phoneNumber = phoneNumberEditText.getText().toString();
                if (isPhoneNumber(phoneNumber)) {
                    new MaterialDialog.Builder(LoginActivity.this)
                            .title(R.string.phone_number_confirmation_title)
                            .content(String.format(getResources().getString(R.string.phone_number_confirmation_message), phoneNumber))
                            .positiveText(R.string.phone_number_confirmation_confirm)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    LoginActivity.this.phoneNumber = phoneNumber;
                                    startVerification();
                                }
                            })
                            .negativeText(R.string.phone_number_confirmation_cancel)
                            .show();
                } else {
                    phoneNumberEditText.setError("Invalid phone number");
                }
            }
        });


    }
    boolean isPhoneNumber(String phoneNumber) {
        phoneNumber = phoneNumber.replace(" ", "");
        return phoneNumber.startsWith("+91") && phoneNumber.length() == 13;
    }

    void startVerification() {
        Intent intent = new Intent(LoginActivity.this, VerifyActivity.class);
        intent.putExtra("phone", phoneNumber);
        startActivity(intent);
    }

}
