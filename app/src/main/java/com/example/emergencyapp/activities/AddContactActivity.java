package com.example.emergencyapp.activities;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.emergencyapp.utils.Contact;
import com.example.emergencyapp.R;

import java.util.ArrayList;
import java.util.List;

public class AddContactActivity extends AppCompatActivity {

    private static final int REQUEST_READ_CONTACTS_PERMISSION = 0;
    private static final int REQUEST_CONTACT = 1;

    private Button mContactPick;
    private Button mContactDelete;
    private ListView mContactList;

    private int selectedPosition = -1;
    private static final String KEY_CONTACT_LIST = "contactList";
    private List<Contact> contactList=new ArrayList<>();;

    private ArrayAdapter<Contact> adapter;

    private static String TAG="EmergencyCall";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contact_activity);

        // Intent to pick contacts
        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

        mContactPick = findViewById(R.id.contact_pick);
        mContactList = findViewById(R.id.contacts_list);
        mContactList.setItemsCanFocus(true);
        mContactDelete = findViewById(R.id.contact_delete);

        mContactPick.setOnClickListener(view -> startActivityForResult(pickContact, REQUEST_CONTACT));
        mContactList.setOnItemClickListener((parent, view, position, id) -> {
            selectedPosition = position;
            updateSelection();
        });

        mContactDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });

        requestContactsPermission();
        updateButton(hasContactsPermission());

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactList);
        mContactList.setAdapter(adapter);
        Log.i(TAG, "onCreate: Add contact activity");
        loadContacts();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Contact.saveContactsToPreferences(this,contactList);
    }


    private void loadContacts() {
        // Load existing contacts from SharedPreferences
        ArrayList<Contact> existingContacts = Contact.loadContactsFromPreferences(this);

        // Add the loaded contacts to the adapter
        if(existingContacts!=null) {
            adapter.addAll(existingContacts);

            // Notify the adapter that the data has changed
            adapter.notifyDataSetChanged();
        }
        Log.i(TAG, "loadContacts: ");
    }

    public void updateButton(boolean enable)
    {
        mContactPick.setEnabled(enable);
        mContactList.setEnabled(enable);
        mContactDelete.setEnabled(false);
    }

    private void showDeleteConfirmationDialog() {
        if (selectedPosition != -1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete Contact");
            builder.setMessage("Are you sure you want to delete this contact?");

            builder.setPositiveButton("Delete", (dialog, which) -> {
                // Delete the selected contact
                deleteContact(selectedPosition);
            });

            builder.setNegativeButton("Cancel", null);

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_READ_CONTACTS_PERMISSION && grantResults.length > 0)
        {
            updateButton(grantResults[0] == PackageManager.PERMISSION_GRANTED);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == REQUEST_CONTACT && data != null)
        {
            String contactID = getContactID(data);
            Contact contact= getContact(contactID);

            if (contact!=null){
                saveContactToPreferences(contact);
            }

        }
    }

    @SuppressLint("Range")
    private String getContactID(Intent data){
        Uri contactUri = data.getData();

        // Specify which fields you want
        // your query to return values for
        String[] queryFields = new String[]{ContactsContract.Contacts._ID};


        // Perform your query - the contactUri
        // is like a "where" clause here
        Cursor cursor = this.getContentResolver()
                .query(contactUri, queryFields, null, null, null);

        String contactID=null;

        if(cursor.moveToFirst()) {
            contactID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        }
        cursor.close();

        return contactID;
    }

    private Contact getContact(String contactID){
        Contact contact=null;
        Cursor cursorPhone = this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);
        try
        {
            if (cursorPhone != null && cursorPhone.moveToFirst()) {
                int nameColumnIndex = cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int phoneNumberColumnIndex = cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                String contactName = cursorPhone.getString(nameColumnIndex);
                String contactPhoneNumber = cursorPhone.getString(phoneNumberColumnIndex);

                Log.d(TAG, "Contact Name: " + contactName);
                Log.d(TAG, "Contact Phone Number: " + contactPhoneNumber);

                contact=new Contact(contactName,contactPhoneNumber);
            }

        }
        finally
        {
            cursorPhone.close();
        }
        return contact;
    }

    private boolean hasContactsPermission()
    {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED;
    }

    // Request contact permission if it
    // has not been granted already
    private void requestContactsPermission()
    {
        if (!hasContactsPermission())
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS_PERMISSION);
        }
    }

    private void saveContactToPreferences(Contact contact) {
        // Load existing contacts from SharedPreferences
        contactList = Contact.loadContactsFromPreferences(this);
        // Add the new contact
        adapter.add(contact);
        contactList.add(contact);
        adapter.notifyDataSetChanged();

        // Save the updated contacts list to SharedPreferences
        Contact.saveContactsToPreferences(this, contactList);

        Log.d(TAG, "saveContactToPreferences: "+contactList.size());
    }

    private void deleteContact(int position) {
        // Remove the contact from the adapter
        adapter.remove(adapter.getItem(position));
        //contactList.remove(position);
        // Notify the adapter that the data has changed
        adapter.notifyDataSetChanged();

        // Save the updated contacts list to SharedPreferences
        Contact.saveContactsToPreferences(this, contactList);

        // Clear selection
        selectedPosition = -1;
        updateSelection();
        Log.d(TAG, "deleteContact: "+contactList.size());
    }

    private void updateSelection() {
        int itemCount = mContactList.getChildCount();
        for (int i = 0; i < itemCount; i++) {
            View view = mContactList.getChildAt(i);
            if(i==selectedPosition){
                view.setBackgroundColor(ContextCompat.getColor(this,R.color.accent_color));
            }else{
                view.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        mContactDelete.setEnabled(selectedPosition != -1);
        Log.i(TAG, "updateSelection");
    }

}
