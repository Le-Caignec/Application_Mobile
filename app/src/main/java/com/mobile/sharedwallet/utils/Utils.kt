package com.mobile.sharedwallet.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.mobile.sharedwallet.MainActivity
import com.mobile.sharedwallet.activity.GoogleSignInActivity
import com.mobile.sharedwallet.constants.FirebaseConstants
import com.mobile.sharedwallet.fragment.PortalFragment
import com.mobile.sharedwallet.models.Cagnotte
import com.mobile.sharedwallet.models.Participant
import com.mobile.sharedwallet.models.User
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import com.mobile.sharedwallet.models.Tributaire as Tributaire

class Utils {

    companion object {

        /**
         * Redirect to login page if not logged in
         */
        fun checkLoggedIn(activity: FragmentActivity) {
            val firebaseUser : FirebaseUser? = FirebaseAuth.getInstance().currentUser
            val user : User? = Shared.user
            if (firebaseUser != null && user != null) {
                if (!user.isNullOrEmpty() && firebaseUser.uid == user.uid) {
                    return
                }
            }
            (activity as MainActivity).replaceFragment(PortalFragment(), false)
        }

        fun getDisplayNameFromFirstnameAndLastName(fN: String?, lN: String?): String {
            return fN
                .plus(FirebaseConstants.String.DisplayNameSeparator)
                .plus(lN)
        }

        private fun getFirstnameAndLastnameFromDisplayName(displayName: String?, googleAuth : Boolean = false) : HashMap<String, String> {
            return if (!displayName.isNullOrEmpty()) {
                val fNAndLN : List<String> = displayName.split(
                    if (!googleAuth) FirebaseConstants.String.DisplayNameSeparator
                    else " "
                )
                hashMapOf(
                    User.Attributes.FIRST_NAME.string to fNAndLN.first(),
                    User.Attributes.LAST_NAME.string to fNAndLN.last()
                )
            } else hashMapOf(
                User.Attributes.FIRST_NAME.string to "",
                User.Attributes.LAST_NAME.string to ""
            )
        }

        suspend fun fetchPhoto(uid : String) : Bitmap? {
            val photoInCache : Bitmap? = Shared.cache.get(uid)
            if(photoInCache != null) return photoInCache

            return try {
                val byteArray : ByteArray = FirebaseStorage
                        .getInstance()
                        .reference
                        .child(buildPicturePathRef(uid))
                        .getBytes(FirebaseConstants.MAX_PROFILE_PHOTO_SIZE)
                        .await()

                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                val photo = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, false)
                Shared.cache.put(uid, photo)
                photo
            } catch (_ : Exception) {
                // If exception is catched then file does not exist on Storage
                null
            }
        }

        suspend fun createUserFromFirebaseUser(firebaseUser : FirebaseUser?, loadPhoto : Boolean = false) : User {
            return firebaseUser?.let {
                val names : HashMap<String, String> = getFirstnameAndLastnameFromDisplayName(it.displayName, it.zzf() == "google.com")
                var photo : Bitmap? = null
                if(loadPhoto) {
                    photo = fetchPhoto(it.uid)
                }
                return@let User(
                    firebaseUser.uid,
                    names[User.Attributes.FIRST_NAME.string],
                    names[User.Attributes.LAST_NAME.string],
                    firebaseUser.email,
                    photo)
            } ?: User()
        }

        fun buildPicturePathRef(uid : String) : String {
            return FirebaseConstants.StorageRef.Pictures
                .plus("/")
                .plus(uid)
                .plus(".png")
        }

        fun castUserToParticipant(user : User) : Participant {
            return user.uid?.let {
                return@let Participant(user.firstName!!, it,0f )
            } ?: Participant()
        }

        fun castParticipantToTributaire(participant: Participant): Tributaire {
            return Tributaire(participant.name,participant.uid,0f,0f,false)
        }

        fun castParticipantListToTributaireList( participant : ArrayList<Participant>):  ArrayList<Tributaire> {
            val tributaire  = ArrayList<Tributaire>()
            for (element in participant){
                tributaire.add(castParticipantToTributaire(element))
            }
            return tributaire
        }

        fun convertStringToRef(collectionName : String, ref : String) : DocumentReference {
            return FirebaseFirestore.getInstance().collection(collectionName).document(ref)
        }

        fun dateFormatter(ts : Timestamp) : String {
            val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.CANADA_FRENCH)
            return formatter.format(ts.toDate()) ?: formatter.format(Date())
        }

        suspend fun loadCagnotteList() {
            // Load all pots current user is involved in
            Shared.user?.let { user : User ->
                withContext(Dispatchers.Main) {
                    Shared.cagnottes = try {
                        val result: QuerySnapshot = FirebaseFirestore
                            .getInstance()
                            .collection(FirebaseConstants.CollectionNames.Pot)
                            .whereArrayContains(Cagnotte.Attributes.UIDS.string, user.uid!!)
                            .get()
                            .await()

                        val lCagnottes: HashMap<String, Cagnotte> = HashMap()
                        for (document in result) {
                            val cagnotte : Cagnotte = document.toObject()
                            lCagnottes[document.id] = cagnotte
                        }
                        if (lCagnottes.size > 1) lCagnottes.toList().sortedByDescending { (_, v) -> v.creationDate }.toMap() as HashMap<String, Cagnotte>
                        else lCagnottes
                    } catch (e: Exception) {
                        HashMap()
                    }
                }
            }
        }

        fun updateUser(act: Activity, user : FirebaseUser?) {
            user?.let {
                MainScope().launch {
                    Shared.user = createUserFromFirebaseUser(it, true)
                    if (act !is MainActivity) {
                        act.finish()
                    }
                    Shared.user?.let { u : User -> (act as MainActivity).checkIfInvitation(u) }
                    Shared.overlay?.hide()
                }
            }
        }
    }
}