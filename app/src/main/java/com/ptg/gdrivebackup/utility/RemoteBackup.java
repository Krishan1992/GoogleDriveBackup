package com.ptg.gdrivebackup.utility;

import android.app.Activity;
import android.content.IntentSender;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityOptions;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.ptg.gdrivebackup.utility.DatabaseHelper.DATABASE_NAME;


public class RemoteBackup {
    public TaskCompletionSource<DriveId> mOpenItemTaskSource;
    private DriveResourceClient mDriveResourceClient;
    private DriveClient mDriveClient;
    private static final String TAG = "Google Drive Activity";
    private Activity activity;
    public static final int REQUEST_CODE_SIGN_IN = 0;
    public static final int REQUEST_CODE_OPENING = 1;
    public static final int REQUEST_CODE_CREATION = 2;
    public RemoteBackup(Activity activity) {
        this.activity = activity;
    }

    public void connectToDrive() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account == null) {
            signIn();
        }
    }

    public boolean isAuthenticated() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account == null) {
            return false;
        } else return true;
    }

    public String getAuthenticatedEmail() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account == null) {
            return null;
        } else
            return account.getEmail();
    }

    public void signIn() {
        Log.i(TAG, "Start sign in");
        GoogleSignInClient GoogleSignInClient = buildGoogleSignInClient();
        activity.startActivityForResult(GoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(Drive.SCOPE_FILE)
                        .build();
        return GoogleSignIn.getClient(activity, signInOptions);
    }

    public void backup() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account == null) {
            signIn();
        } else {
            mDriveClient = Drive.getDriveClient(activity, account);
            // Build a drive resource client.
            mDriveResourceClient = Drive.getDriveResourceClient(activity, account);
            mDriveResourceClient
                    .createContents()
                    .continueWithTask(
                            task -> createFileIntentSender(task.getResult()))
                    .addOnFailureListener(
                            e -> Log.w(TAG, "Failed to create new contents.", e));
        }
    }

    private Task<Void> createFileIntentSender(DriveContents driveContents) {
        final String inFileName = activity.getDatabasePath(DATABASE_NAME).toString();
        try {
            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);
            OutputStream outputStream = driveContents.getOutputStream();

            // Transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                .setTitle(DATABASE_NAME)
                .setMimeType("application/db")
                .build();


        CreateFileActivityOptions createFileActivityOptions =
                new CreateFileActivityOptions.Builder()
                        .setInitialMetadata(metadataChangeSet)
                        .setInitialDriveContents(driveContents)
                        .build();

        return mDriveClient
                .newCreateFileActivityIntentSender(createFileActivityOptions)
                .continueWith(
                        task -> {
                            activity.startIntentSenderForResult(task.getResult(), REQUEST_CODE_CREATION, null, 0, 0, 0);
                            return null;
                        });
    }

    public void restore() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account == null) {
            signIn();
        } else {
            mDriveClient = Drive.getDriveClient(activity, account);
            // Build a drive resource client.
            mDriveResourceClient = Drive.getDriveResourceClient(activity, account);
            pickFile()
                    .addOnSuccessListener(activity,
                            driveId -> retrieveContents(driveId.asDriveFile()))
                    .addOnFailureListener(activity, e -> {
                        Log.e(TAG, "No file selected", e);
                    });
        }
    }

    private Task<DriveId> pickFile() {
        OpenFileActivityOptions openOptions =
                new OpenFileActivityOptions.Builder()
                        .setSelectionFilter(Filters.eq(SearchableField.MIME_TYPE, "application/db"))
                        .setActivityTitle("Select DB File")
                        .build();
        return pickItem(openOptions);
    }

    private Task<DriveId> pickItem(OpenFileActivityOptions openOptions) {
        mOpenItemTaskSource = new TaskCompletionSource<>();
        mDriveClient
                .newOpenFileActivityIntentSender(openOptions)
                .continueWith((Continuation<IntentSender, Void>) task -> {
                    activity.startIntentSenderForResult(
                            task.getResult(), REQUEST_CODE_OPENING, null, 0, 0, 0);
                    return null;
                });
        return mOpenItemTaskSource.getTask();
    }

    private void retrieveContents(DriveFile file) {

        //DB Path
        final String inFileName = activity.getDatabasePath(DATABASE_NAME).toString();

        Task<DriveContents> openFileTask = mDriveResourceClient.openFile(file, DriveFile.MODE_READ_ONLY);

        openFileTask
                .continueWithTask(task -> {
                    DriveContents contents = task.getResult();
                    try {
                        ParcelFileDescriptor parcelFileDescriptor = contents.getParcelFileDescriptor();
                        FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());

                        // Open the empty db as the output stream
                        OutputStream output = new FileOutputStream(inFileName);

                        // Transfer bytes from the inputfile to the outputfile
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fileInputStream.read(buffer)) > 0) {
                            output.write(buffer, 0, length);
                        }

                        // Close the streams
                        output.flush();
                        output.close();
                        fileInputStream.close();
                        Toast.makeText(activity, "Import completed", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(activity, "Error on import", Toast.LENGTH_SHORT).show();
                    }
                    return mDriveResourceClient.discardContents(contents);

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Unable to read contents", e);
                    Toast.makeText(activity, "Error on import", Toast.LENGTH_SHORT).show();
                });
    }
}
