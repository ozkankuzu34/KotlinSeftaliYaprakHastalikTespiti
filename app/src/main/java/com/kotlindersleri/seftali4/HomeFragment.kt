package com.kotlindersleri.seftali4

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.compose.ui.window.application
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.kotlindersleri.seftali4.HomeFragment.Companion
import com.kotlindersleri.seftali4.databinding.FragmentHomeBinding
import com.kotlindersleri.seftali4.ml.Modelinceptionv3
import com.kotlindersleri.seftali4.ml.Projemodel7ocak
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private var selectedImageUri: Uri? = null
    private lateinit var bitmap: Bitmap
    private lateinit var photoUri: Uri

    companion object {
        private const val REQUEST_GALLERY = 100
        private const val REQUEST_CAMERA = 101
        private const val CAMERA_PERMISSION_CODE = 102
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)



        binding.btnSelect.setOnClickListener {
            // Kullanıcıya galeri veya kamera seçeneği sun
            val options = arrayOf("Galeriden Seç", "Kamera ile Fotoğraf Çek")
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Resim Kaynağını Seçin")
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> openGallery() // Galeri seçildi
                    1 -> checkCameraPermission() // Kamera izni kontrol et
                }
            }
            builder.show()
        }

        // TensorFlow işlemleri
        val labels = requireContext().assets.open("labels.txt").bufferedReader().readLines()

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(299, 299, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f))
            .build()

        binding.btnPredict.setOnClickListener {
            val tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(bitmap)

            val processedImage = imageProcessor.process(tensorImage)

            val model = Modelinceptionv3.newInstance(requireContext())

            // Creates inputs for reference.
            val inputFeature0 =
                TensorBuffer.createFixedSize(intArrayOf(1, 299, 299, 3), DataType.FLOAT32)
            inputFeature0.loadBuffer(processedImage.buffer)

            // Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

            var maxIds = 0
            outputFeature0.forEachIndexed { index, fl ->
                if (outputFeature0[maxIds] < fl) {
                    maxIds = index
                }
            }
            //binding.textPrediction.text = labels[maxIds]

            //val hastalik = labels[maxIds]

            // En yüksek tahmin değerini ve indeksini bul
            val maxIndex = outputFeature0.indices.maxByOrNull { outputFeature0[it] } ?: -1
            val confidence = if (maxIndex != -1) outputFeature0[maxIndex] * 100 else 0.0f

// Etiketleri eşleştir ve sonucu göster
            /*if (maxIndex != -1) {
                if (confidence>=90){
                val predictedLabel = labels[maxIndex]
                binding.textPrediction.text = "Tahmin: $predictedLabel, Güven: %.2f%%".format(confidence)
                    // Hastalığa göre tedavi yazdır
                    when (predictedLabel) {
                        "Yaprak Kıvırcıklığı Hastalığı" -> binding.tedaviTxt.text =
                            "Tedavi: Yaprak dökümünden hemen sonra veya erken ilkbaharda, tomurcuklar kabarmadan önce ağaçlara bakırlı fungisit uygulayın."
                        "Bakteriyel Leke Hastalığı" -> binding.tedaviTxt.text =
                            "Tedavi: Özellikle meyve döneminde etkili olmak için antibakteriyel etkili ticari ilaçlar kullanılabilir. Kullanım talimatlarına uygun şekilde uygulanmalıdır."
                    }}

                else {
                    binding.textPrediction.text = "Bu yaprak resmi olmayabilir"
                    binding.tedaviTxt.text =""
                }

            } else {
                binding.textPrediction.text = "Tahmin yapılamadı!"
            }*/
            if (maxIndex != -1) {
                if (confidence >= 95) {
                    val predictedLabel = labels[maxIndex]
                    binding.textPrediction.text = "Tahmin: $predictedLabel, Güven: %.2f%%".format(confidence)

                    // Hastalığa uygun tedavi önerisini belirle
                    binding.tedaviTxt.text = when (predictedLabel) {
                        "Yaprak Kıvırcıklığı Hastalığı" -> "Tedavi: Yaprak dökümünden hemen sonra veya erken ilkbaharda, tomurcuklar kabarmadan önce ağaçlara bakırlı fungisit uygulayın."
                        "Bakteriyel Leke Hastalığı" -> "Tedavi: Özellikle meyve döneminde etkili olmak için antibakteriyel etkili ticari ilaçlar kullanılabilir. Kullanım talimatlarına uygun şekilde uygulanmalıdır."
                        else -> ""
                    }
                } else {
                    // Düşük güven oranı veya farklı bir durum için mesaj
                    binding.textPrediction.text = "Bu yaprak resmi olmayabilir"
                    binding.tedaviTxt.text = ""
                }
            } else {
                // Tahmin yapılamadıysa gösterilecek mesaj
                binding.textPrediction.text = "Tahmin yapılamadı!"
                binding.tedaviTxt.text = ""
            }



            // Releases model resources if no longer used.
            model.close()
        }
        return binding.root
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, HomeFragment.REQUEST_GALLERY)
    }

    private fun openCamera() {
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile("photo_", ".jpg", storageDir)
        photoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        startActivityForResult(intent, HomeFragment.REQUEST_CAMERA)
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Kamera izni yok, kullanıcıdan izin iste
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA),
                HomeFragment.CAMERA_PERMISSION_CODE
            )
        } else {
            // İzin zaten verilmiş, kamerayı aç
            openCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == HomeFragment.CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera() // İzin verildiyse kamerayı aç
            } else {
                Toast.makeText(requireContext(), "Kamera izni reddedildi!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == HomeFragment.REQUEST_GALLERY && resultCode == RESULT_OK && data != null) {
            // Galeriden resim seçildi
            selectedImageUri = data.data
            if (selectedImageUri != null) {
                bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, selectedImageUri)
                binding.imageView.setImageBitmap(bitmap)
            }
        } else if (requestCode == HomeFragment.REQUEST_CAMERA && resultCode == RESULT_OK) {
            // Kamera ile fotoğraf çekildi
            bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, photoUri)
            binding.imageView.setImageBitmap(bitmap)
        }
    }
}

