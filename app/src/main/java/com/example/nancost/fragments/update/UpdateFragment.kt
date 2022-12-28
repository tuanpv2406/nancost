package com.example.nancost.fragments.update

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.room.util.StringUtil
import com.example.nancost.R
import com.example.nancost.databinding.FragmentUpdateBinding
import com.example.nancost.fragments.dialog.ActionDialog
import com.example.nancost.model.Nancost
import com.example.nancost.model.NancostData
import com.example.nancost.utils.AppConstant
import com.example.nancost.utils.SharedPreUtils
import com.example.nancost.utils.StringUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class UpdateFragment : Fragment() {

    private val args by navArgs<UpdateFragmentArgs>()

    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!
    private val nancostDataList: ArrayList<NancostData?> = arrayListOf()
    private var indexMatched = 0
    var userUid: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        val view = binding.root
        userUid = SharedPreUtils.getString(AppConstant.Enum.USER_UID)

        binding.deliveredLeavesContent.setText(args.currentNancost?.deliveredLeaves.toString())
        binding.deliveredVolumeContent.setText(args.currentNancost?.deliveredVolume.toString())

        binding.btnUpdate.setOnClickListener {
            ActionDialog.show( childFragmentManager,
                "Cập nhật",
                "Bạn có chắc chắn với những thông tin cần cập nhật?"
            ).apply {
                onNegativeActionListener = {
                    dismiss()
                }
                onPositiveActionListener = {
                    updateItem()
                }
            }
        }

        setHasOptionsMenu(true)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Firebase.database.getReference("$userUid/nancost/${args.currentNancost?.nancostUid}/nancostDataList/")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (ds in snapshot.children) {
                            val data: NancostData? = ds.getValue(NancostData::class.java)
                            nancostDataList.add(data)
                        }
                        nancostDataList.forEachIndexed { index, item ->
                            if (item?.nancostDataUid == args.currentNancost?.nancostDataUid) {
                                indexMatched = index
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        binding.unitPriceContent.setText(StringUtils.formatCurrency(args.currentNancost?.unitPrice ?: 0))
        binding.amountPayContent.setText(StringUtils.formatCurrency(args.currentNancost?.amountWillPay?: 0))
        binding.amountPaidContent.setText(StringUtils.formatCurrency((args.currentNancost?.amountPaid ?: 0)))
    }

    private fun updateItem() {
        val deliveredLeavesContent = binding.deliveredLeavesContent.text.toString()
        val deliveredVolumeContent = binding.deliveredVolumeContent.text.toString()
        val unitPrice = args.currentNancost?.unitPrice
        val amountPaidContent = StringUtils.currencyToString(binding.amountPaidContent.text.toString())

        if (inputCheck(deliveredLeavesContent, deliveredVolumeContent, amountPaidContent)) {
            val nancostData = NancostData(
                nancostDataUid = args.currentNancost?.nancostDataUid,
                nancostUid = args.currentNancost?.nancostUid,
                deliveredLeaves = deliveredLeavesContent.toInt(),
                deliveredVolume = deliveredVolumeContent.toDouble(),
                unitPrice = unitPrice,
                amountPaid = amountPaidContent.toInt()
            )
            nancostData.amountWillPay = nancostData.getAmountPay()

            Firebase.database.getReference("$userUid/nancost/${args.currentNancost?.nancostUid}/nancostDataList/${indexMatched}")
                .setValue(nancostData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Cập nhật thất bại!", Toast.LENGTH_LONG).show()
                }

            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        } else {
            binding.tvError.visibility = View.VISIBLE
            binding.tvError.text = getString(R.string.str_register_notice_msg)
        }
    }

    private fun inputCheck(
        deliveredLeaves: String,
        deliveredVolume: String,
        amountPaid: String
    ): Boolean {
        return (deliveredLeaves.isNotBlank() && deliveredVolume.isNotBlank() && amountPaid.isNotBlank())
    }
}