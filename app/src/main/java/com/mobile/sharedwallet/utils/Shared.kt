package com.mobile.sharedwallet.utils

import com.mobile.sharedwallet.fragment.CagnotteFragment
import com.mobile.sharedwallet.models.Cagnotte
import com.mobile.sharedwallet.models.Participant
import com.mobile.sharedwallet.models.Tributaire
import com.mobile.sharedwallet.models.User

class Shared {

    companion object {
        var overlay : Overlay? = null

        var cache = CachePhoto()

        var user : User? = null
            get() {
                if (field == null || field!!.isNullOrEmpty())
                    return User()
                return field
            }

        var cagnottes : HashMap<String, Cagnotte> = HashMap()

        var pot : Cagnotte = Cagnotte()
            get() {
                if (field.isEmpty()) {
                    field = Cagnotte()
                }
                return field
            }

        var potRef : String = ""

        var cagnotteFragment : CagnotteFragment? = null

        var payeur : Participant? = null
    }
}