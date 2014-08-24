package com.repositorysentry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class CreateSentryActivity extends Activity {

	private EditText mUsernameText;
	private EditText mRepositoryText;
	private Spinner mSpinnerVcs;

	private static int DEFAULT_REPO_TYPE = 0;

	public static final String INTENT_KEY_SENTRY = "SentryItem";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_alarm);

		mSpinnerVcs = (Spinner) findViewById(R.id.spinnerVcs);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.vcs, R.layout.vsc_dropdown_item);
		mSpinnerVcs.setAdapter(adapter);
		mSpinnerVcs.setSelection(DEFAULT_REPO_TYPE);

		mUsernameText = (EditText) findViewById(R.id.edittext_username);
		mRepositoryText = (EditText) findViewById(R.id.edittext_repository);

		// OnClickListener for Clear All Button
		final Button buttonClear = (Button) findViewById(R.id.button_clear);
		buttonClear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mUsernameText.setText("");
				mRepositoryText.setText("");
			}
		});

		// OnClickListener for Set Alarm Button
		final Button buttonSetAlarm = (Button) findViewById(R.id.button_set_alarm);
		buttonSetAlarm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String username = mUsernameText.getText().toString();
				String repositoryName = mRepositoryText.getText().toString();
				String repoType = mSpinnerVcs.getSelectedItem().toString();

				SentryCreator sentryCreator = new SentryCreator(
						getApplicationContext(), repoType, username,
						repositoryName);
				Repository repo = sentryCreator.create();

				Intent intent = new Intent();
				intent.putExtra(INTENT_KEY_SENTRY, repo);
				setResult(RESULT_OK, intent);

				finish();
			}
		});

	}
}
