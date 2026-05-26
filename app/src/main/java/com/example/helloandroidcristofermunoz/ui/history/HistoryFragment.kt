package com.example.helloandroidcristofermunoz.ui.history

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import com.example.helloandroidcristofermunoz.databinding.FragmentHistoryBinding
import com.example.helloandroidcristofermunoz.ui.home.TransactionAdapter
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.pdf.draw.LineSeparator
import java.util.Date
import java.text.SimpleDateFormat

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private var _binding: FragmentHistoryBinding? = null
    private val binding
        get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).transactionDao()
        val repository = TransactionRepository(dao)
        HistoryViewModelFactory(repository)
    }

    private val monthNames =
            arrayOf(
                    "Enero",
                    "Febrero",
                    "Marzo",
                    "Abril",
                    "Mayo",
                    "Junio",
                    "Julio",
                    "Agosto",
                    "Septiembre",
                    "Octubre",
                    "Noviembre",
                    "Diciembre"
            )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHistoryBinding.bind(view)

        setupMonthSelector()
        setupRecycler()
        observeData()

        binding.btnExportPdf.setOnClickListener { exportPdf() }
    }

    private fun setupMonthSelector() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        val monthText = "${monthNames[currentMonth]} $currentYear"
        binding.monthSelector.removeAllViews()

        val monthButton =
                android.widget.Button(requireContext()).apply {
                    text = monthText
                    textSize = 16f
                    setPadding(32, 16, 32, 16)
                    setBackgroundColor(Color.parseColor("#2196F3"))
                    setTextColor(Color.WHITE)
                    isAllCaps = false

                    setOnClickListener { showMonthYearPicker() }
                }

        binding.monthSelector.addView(monthButton)
    }

    private fun showMonthYearPicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        val datePickerDialog =
                DatePickerDialog(
                        requireContext(),
                        { _, selectedYear, selectedMonth, _ ->
                            viewModel.selectMonth(selectedMonth, selectedYear)
                            updateMonthSelectorUI(selectedMonth, selectedYear)
                        },
                        year,
                        month,
                        1
                )

        // Configurar para mostrar solo mes y año
        try {
            val datePicker = datePickerDialog.datePicker
            val dayField =
                    datePicker.findViewById<View>(resources.getIdentifier("day", "id", "android"))
            if (dayField != null) {
                dayField.visibility = View.GONE
            }
        } catch (e: Exception) {
            // Si no se puede ocultar el día, se muestra normal
        }

        datePickerDialog.show()
    }

    private fun updateMonthSelectorUI(selectedMonth: Int, selectedYear: Int) {
        val monthText = "${monthNames[selectedMonth]} $selectedYear"
        val button = binding.monthSelector.getChildAt(0) as android.widget.Button
        button.text = monthText
    }

    private fun setupRecycler() {
        binding.recyclerHistory.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeData() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            val adapter = TransactionAdapter(transactions, onItemClick = {})
            binding.recyclerHistory.adapter = adapter
        }

        viewModel.monthIncome.observe(viewLifecycleOwner) { income ->
            val formatted = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(income)
            binding.txtMonthIncome.text = formatted
        }

        viewModel.monthExpenses.observe(viewLifecycleOwner) { expenses ->
            val formatted = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(expenses)
            binding.txtMonthExpenses.text = formatted
        }

        viewModel.monthBalance.observe(viewLifecycleOwner) { balance ->
            val formatted = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(balance)
            binding.txtMonthBalance.text = formatted

            binding.txtMonthBalance.setTextColor(
                    if (balance >= 0) Color.parseColor("#212121") else Color.parseColor("#F44336")
            )
        }
    }
    private fun exportPdf() {

        try {

            val document = Document()

            val fileName = "Historial_${System.currentTimeMillis()}.pdf"

            val file =
                    File(
                            requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                            fileName
                    )

            PdfWriter.getInstance(document, FileOutputStream(file))

            document.open()

            // ===== FUENTES =====

            val titleFont = Font(Font.FontFamily.HELVETICA, 24f, Font.BOLD, BaseColor(46, 125, 50))

            val subtitleFont = Font(Font.FontFamily.HELVETICA, 14f, Font.NORMAL, BaseColor.GRAY)

            val sectionFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD, BaseColor(33, 33, 33))

            val normalFont = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL, BaseColor.DARK_GRAY)

            val incomeFont = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD, BaseColor(76, 175, 80))

            val expenseFont =
                    Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD, BaseColor(244, 67, 54))

            // ===== TITULO =====

            val title = Paragraph("FinanTrack", titleFont)
            title.alignment = Element.ALIGN_CENTER
            title.spacingAfter = 6f

            document.add(title)

            val subtitle = Paragraph("Reporte financiero mensual", subtitleFont)

            subtitle.alignment = Element.ALIGN_CENTER
            subtitle.spacingAfter = 30f

            document.add(subtitle)

            // ===== FECHA =====

            val currentDate =
                    java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CO"))
                            .format(Date())

            val generated = Paragraph("Generado el: $currentDate", normalFont)

            generated.spacingAfter = 25f

            document.add(generated)

            // ===== RESUMEN =====

            val resumen = Paragraph("Resumen del Mes", sectionFont)

            resumen.spacingAfter = 15f

            document.add(resumen)

            val income = binding.txtMonthIncome.text.toString()
            val expenses = binding.txtMonthExpenses.text.toString()
            val balance = binding.txtMonthBalance.text.toString()

            val incomeParagraph = Paragraph("Ingresos: $income", incomeFont)

            incomeParagraph.spacingAfter = 8f

            val expenseParagraph = Paragraph("Gastos: $expenses", expenseFont)

            expenseParagraph.spacingAfter = 8f

            val balanceParagraph = Paragraph("Balance Total: $balance", sectionFont)

            balanceParagraph.spacingAfter = 30f

            document.add(incomeParagraph)
            document.add(expenseParagraph)
            document.add(balanceParagraph)

            // ===== TRANSACCIONES =====

            val transactionsTitle = Paragraph("Transacciones", sectionFont)

            transactionsTitle.spacingAfter = 15f

            document.add(transactionsTitle)

            val transactions = viewModel.transactions.value ?: emptyList()

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CO"))

            transactions.forEachIndexed { index, transaction ->
                val symbol = if (transaction.type == "Ingreso") "＋" else "－"

                val amount =
                        NumberFormat.getCurrencyInstance(Locale("es", "CO"))
                                .format(transaction.amount)

                val transactionFont = if (transaction.type == "Ingreso") incomeFont else expenseFont

                val cardTitle = Paragraph("${index + 1}. ${transaction.title}", sectionFont)

                cardTitle.spacingAfter = 4f

                document.add(cardTitle)

                document.add(Paragraph("Categoría: ${transaction.category}", normalFont))

                document.add(Paragraph("Tipo: ${transaction.type}", normalFont))

                document.add(
                        Paragraph(
                                "Fecha: ${
                        dateFormat.format(
                            Date(transaction.date)
                        )
                    }",
                                normalFont
                        )
                )

                val amountParagraph = Paragraph("Monto: $symbol $amount", transactionFont)

                amountParagraph.spacingAfter = 15f

                document.add(amountParagraph)

                // Línea separadora
                val separator = LineSeparator()
                separator.lineColor = BaseColor(220, 220, 220)

                document.add(separator)

                document.add(Paragraph(" "))
            }

            // ===== FOOTER =====

            val footer = Paragraph("Generado automáticamente por FinanTrack", subtitleFont)

            footer.alignment = Element.ALIGN_CENTER
            footer.spacingBefore = 30f

            document.add(footer)

            document.close()

            showPdfNotification(file)

            Toast.makeText(requireContext(), "PDF exportado correctamente", Toast.LENGTH_LONG)
                    .show()
        } catch (e: Exception) {

            e.printStackTrace()

            Toast.makeText(requireContext(), "Error al exportar PDF", Toast.LENGTH_LONG).show()
        }
    }
    private fun showPdfNotification(file: File) {

        val channelId = "pdf_channel"

        val notificationManager = requireContext().getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel =
                    NotificationChannel(
                            channelId,
                            "PDF Downloads",
                            NotificationManager.IMPORTANCE_HIGH
                    )

            notificationManager.createNotificationChannel(channel)
        }

        val uri: Uri =
                FileProvider.getUriForFile(
                        requireContext(),
                        "${requireContext().packageName}.provider",
                        file
                )

        val intent =
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                }

        val pendingIntent =
                PendingIntent.getActivity(
                        requireContext(),
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

        val notification =
                NotificationCompat.Builder(requireContext(), channelId)
                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                        .setContentTitle("PDF descargado")
                        .setContentText("Toca para abrir el archivo")
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build()

        notificationManager.notify(1, notification)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
