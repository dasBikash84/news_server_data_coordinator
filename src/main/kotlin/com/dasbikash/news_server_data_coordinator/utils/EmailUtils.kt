package com.dasbikash.news_server_data_coordinator.utils

import com.dasbikash.news_server_data_coordinator.model.EmailAuth
import com.google.gson.Gson
import java.io.FileReader
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailUtils {
    val emailAuth:EmailAuth

    init {
        val reader = FileReader("src/main/resources/email_details.json")
        emailAuth = Gson().fromJson(reader,EmailAuth::class.java)
    }

    fun sendEmail(subject:String,body:String):Boolean{

        val prop = Properties()

        emailAuth.properties!!.keys.asSequence().forEach {
            prop.put(it, emailAuth.properties!!.get(it))
        }

        val session = Session.getInstance(prop,
                object : javax.mail.Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(emailAuth.userName, emailAuth.passWord)
                    }
                })

        try {

            val message = MimeMessage(session)
            message.setFrom(InternetAddress(emailAuth.userName))
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(emailAuth.toAddresses)
            )
            message.subject = subject
            message.setText(body)

            Transport.send(message)

            return true

        } catch (e: MessagingException) {
            e.printStackTrace()
            return false
        }
    }
}

