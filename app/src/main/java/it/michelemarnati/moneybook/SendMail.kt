package it.michelemarnati.moneybook

import javax.mail.Session;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


//Class is extending AsyncTask because this class is going to perform a networking operation
class SendMail: AsyncTask<Void,Void,Void> {

    //Declaring Variables
    private lateinit var context: Context;
    private lateinit var session: Session;

    //Information to send email
    private var email: String;
    private var subject: String;
    private var message: String;

    //Progressdialog to show while sending email
    private lateinit var progressDialog: ProgressDialog;

    //Class Constructor
    constructor (context: Context, email: String, subject: String, message: String) {
        //Initializing variables
        this.context = context;
        this.email = email;
        this.subject = subject;
        this.message = message;
    }

    override fun onPreExecute() {
        super.onPreExecute();
        //Showing progress dialog while sending email
        progressDialog =
            ProgressDialog.show(context, "Sto inviando il codice", "Attendere...", false, false);
    }

    override fun onPostExecute(aVoid: Void?) {
        super.onPostExecute(aVoid);
        //Dismissing the progress dialog
        progressDialog.dismiss();
        //Showing a success message
        Toast.makeText(context, "Codice inviato!", Toast.LENGTH_LONG).show();
    }

    override fun doInBackground(vararg params: Void?): Void? {

        //Creating properties
        var props = Properties();

        //Configuring properties for gmail
        //If you are not using gmail you may need to change the values
        props["mail.smtp.host"] = "smtp.gmail.com";
        props["mail.smtp.port"] = "465";
        props["mail.smtp.socketFactory.port"] = "465";
        props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory";
        props["mail.smtp.auth"] = "true";




        //Creating a new session
        session = Session.getDefaultInstance(props,


            object : javax.mail.Authenticator() {
                //Authenticating the password
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(Config.EMAIL, Config.PASSWORD);
                }
            });
        try {
            //Creating MimeMessage object
            var mm = MimeMessage(session);

            //Setting sender address
            mm.setFrom(InternetAddress(Config.EMAIL));
            //Adding receiver
            mm.setRecipient(Message.RecipientType.TO, InternetAddress(email));
            //Adding subject
            mm.setSubject(subject);
            //Adding message
            mm.setText(message);

            //Sending email
            Transport.send(mm);


        } catch (e: MessagingException) {
            e.printStackTrace();
        }
        return null;

    }


}
