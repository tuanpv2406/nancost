package com.example.nancost.fragments.update

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nancost.R
import com.example.nancost.databinding.FragmentUpdateBinding
import com.example.nancost.model.Nancost
import com.example.nancost.model.NancostData
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.receivedVolumeContent.setText(args.currentNancost?.receivedVolume.toString())
        binding.deliveredLeavesContent.setText(args.currentNancost?.deliveredLeaves.toString())
        binding.deliveredVolumeContent.setText(args.currentNancost?.deliveredVolume.toString())

        Firebase.database.getReference("nancost/${args.currentNancost?.nancostUid}/nancostDataList/")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (ds in snapshot.children) {
                            val data: NancostData? = ds.getValue(NancostData::class.java)
                            nancostDataList.add(data)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        nancostDataList.forEachIndexed { index, item ->
            if (item?.nancostDataUid == args.currentNancost?.nancostDataUid) {
                indexMatched = index
            }
        }

        binding.btnUpdate.setOnClickListener {
            updateItem()
        }

        setHasOptionsMenu(true)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.remainingVolumeContent.text = args.currentNancost?.getRemainingVolume().toString()
        binding.unitPriceContent.text = args.currentNancost?.unitPrice.toString()
        binding.amoutPayContent.text = args.currentNancost?.getAmountPay().toString()
    }

    private fun updateItem() {
        val receivedVolumeContent = binding.receivedVolumeContent.text
        val deliveredLeavesContent = binding.deliveredLeavesContent.text
        val deliveredVolumeContent = binding.deliveredVolumeContent.text

        if (inputCheck(
                receivedVolumeContent, deliveredLeavesContent, deliveredVolumeContent
            )
        ) {
            val nancostData = NancostData(
                args.currentNancost?.nancostDataUid,
                args.currentNancost?.nancostUid,
                receivedVolumeContent.toString().toDouble(),
                deliveredLeavesContent.toString().toInt(),
                deliveredVolumeContent.toString().toDouble()
            )
            nancostData.getRemainingVolume()
            nancostData.getAmountPay()

            Firebase.database.getReference("nancost/${args.currentNancost?.nancostUid}/nancostDataList/${indexMatched}")
                .setValue(nancostData)

            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        } else {
            Toast.makeText(requireContext(), "Hãy điền hết tất cả các trường...", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun inputCheck(
        receivedVolume: Editable, deliveredLeaves: Editable, deliveredVolume: Editable
    ): Boolean {
        return !(receivedVolume.isEmpty() && deliveredLeaves.isEmpty() && deliveredVolume.isEmpty())
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete) {
            deleteNancost()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteNancost() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Có") { _, _ ->
            Firebase.database.getReference("nancost/${args.currentNancost?.nancostUid}/nancostDataList/${indexMatched}")
                .removeValue()
            Toast.makeText(
                requireContext(), "Đã xóa?", Toast.LENGTH_SHORT
            ).show()
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }
        builder.setNegativeButton("Không") { _, _ -> }
        builder.setTitle("Xóa?")
        builder.setMessage("Bạn đã chắc chắn xóa chưa?")
        builder.create().show()
    }

}