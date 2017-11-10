package pro.postaru.sandu.nearbychat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import pro.postaru.sandu.nearbychat.R;
import pro.postaru.sandu.nearbychat.utils.DataValidator;

public class RegisterFragment extends Fragment {

    private OnFragmentInteractionListener activity;

    private EditText usernameView;
    private EditText emailView;
    private EditText password1View;
    private EditText password2View;

    public RegisterFragment() {
        // Required empty public constructor
    }

    public static RegisterFragment newInstance() {
        RegisterFragment fragment = new RegisterFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register, container, false);

        Button registerButton = (Button) view.findViewById(R.id.register_btn);
        registerButton.setOnClickListener(view1 -> registerHandle());

        usernameView = (EditText) view.findViewById(R.id.register_username);
        usernameView.requestFocus();

        emailView = (EditText) view.findViewById(R.id.register_email);

        password1View = (EditText) view.findViewById(R.id.register_pass_1);

        password2View = (EditText) view.findViewById(R.id.register_pass_2);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            activity = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    private void registerHandle() {

        usernameView.setError(null);
        emailView.setError(null);
        password1View.setError(null);
        password2View.setError(null);

        String userName = usernameView.getText().toString();
        String userEmail = emailView.getText().toString();
        String userPassword1 = password1View.getText().toString();
        String userPassword2 = password2View.getText().toString();

        View errorView = null;

        if (TextUtils.isEmpty(userName)) {
            usernameView.setError(getString(R.string.error_field_required));
            errorView = usernameView;
        } else if (!DataValidator.isUsernameValid(userName)) {
            usernameView.setError(getString(R.string.error_invalid_username));
            errorView = usernameView;
        }

        if (!DataValidator.isPasswordValid(userPassword1)) {
            password1View.setError(getString(R.string.error_invalid_password));
            errorView = password1View;
        }

        if (!DataValidator.isPasswordValid(userPassword2)) {
            password1View.setError(getString(R.string.error_invalid_password));
            errorView = password1View;
        }

        if (!userPassword1.equals(userPassword2)) {
            password1View.setError(getString(R.string.error_different_passwords));
            errorView = password1View;
        }

        if (TextUtils.isEmpty(userEmail) || !DataValidator.isEmailVaid(userEmail)) {
            emailView.setError(getString(R.string.error_invalid_email));
            errorView = emailView;
        }


        if (errorView != null) {
            errorView.requestFocus();
        } else {
            activity.requestRegister(userName, userEmail, userPassword1);
        }
    }


    public interface OnFragmentInteractionListener {
        void requestRegister(String username, String email, String password);

    }
}
