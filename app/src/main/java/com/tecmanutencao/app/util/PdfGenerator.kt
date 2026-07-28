package com.tecmanutencao.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.tecmanutencao.app.R
import com.tecmanutencao.app.domain.model.Cliente
import com.tecmanutencao.app.domain.model.EmpresaConfig
import com.tecmanutencao.app.domain.model.Equipamento
import com.tecmanutencao.app.domain.model.Orcamento
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN

    suspend fun generateOrcamentoPdf(
        context: Context,
        orcamento: Orcamento,
        cliente: Cliente,
        equipamento: Equipamento,
        empresaConfig: EmpresaConfig?
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo_empresa)

        var y = MARGIN

        y = drawHeader(canvas, empresaConfig, orcamento, y, logoBitmap)
        y = drawSectionTitle(canvas, "DADOS DO CLIENTE", y)
        y = drawClientData(canvas, cliente, y)
        y = drawSectionTitle(canvas, "EQUIPAMENTO", y)
        y = drawEquipmentData(canvas, equipamento, y)
        y = drawSectionTitle(canvas, "SERVIÇO", y)
        y = drawServiceData(canvas, orcamento, y)
        y = drawSignatureArea(canvas, y)
        drawFooter(canvas, empresaConfig)

        document.finishPage(page)

        val dir = File(context.filesDir, Constants.PDF_DIR)
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "${Constants.PDF_FILE_PREFIX}${orcamento.numeroOrcamento}${Constants.PDF_FILE_EXTENSION}")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }

    private fun drawHeader(canvas: Canvas, config: EmpresaConfig?, orcamento: Orcamento, startY: Float, logo: Bitmap?): Float {
        val y = startY
        val headerHeight = 100f

        val headerPaint = Paint().apply {
            color = Color.parseColor("#3C50A0")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, y - MARGIN, PAGE_WIDTH.toFloat(), y + headerHeight, headerPaint)

        // Draw logo
        var textStartX = MARGIN
        if (logo != null) {
            val logoSize = 70f
            val logoScale = logoSize / maxOf(logo.width.toFloat(), logo.height.toFloat())
            val logoW = (logo.width * logoScale).toInt()
            val logoH = (logo.height * logoScale).toInt()
            val logoX = MARGIN
            val logoY = y - MARGIN + (headerHeight - logoSize) / 2f
            val scaledLogo = Bitmap.createScaledBitmap(logo, logoW, logoH, true)
            canvas.drawBitmap(scaledLogo, logoX, logoY, null)
            textStartX = logoX + logoW + 15f
        }

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val title = config?.nomeEmpresa?.ifEmpty { "ACM@TECH INFORMÁTICA" } ?: "ACM@TECH INFORMÁTICA"
        canvas.drawText(title, textStartX, y + 8f, titlePaint)

        val infoPaint = Paint().apply {
            color = Color.WHITE
            textSize = 9f
            isAntiAlias = true
        }

        var infoY = y + 24f
        if (config != null) {
            if (config.cnpj.isNotEmpty()) { canvas.drawText("CNPJ: ${config.cnpj}", textStartX, infoY, infoPaint); infoY += 12f }
            if (config.telefone.isNotEmpty()) { canvas.drawText("Tel: ${config.telefone}", textStartX, infoY, infoPaint); infoY += 12f }
            if (config.whatsapp.isNotEmpty()) { canvas.drawText("WhatsApp: ${config.whatsapp}", textStartX, infoY, infoPaint); infoY += 12f }
            if (config.email.isNotEmpty()) { canvas.drawText("E-mail: ${config.email}", textStartX, infoY, infoPaint); infoY += 12f }
            if (config.endereco.isNotEmpty()) {
                canvas.drawText("${config.endereco}, ${config.cidade} - ${config.estado}", textStartX, infoY, infoPaint)
            }
        } else {
            infoY += 12f
            canvas.drawText("Sistema de Orçamentos", textStartX, infoY, infoPaint)
        }

        val orcInfoPaint = Paint().apply {
            color = Color.WHITE
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val label = "ORÇAMENTO"
        val labelWidth = orcInfoPaint.measureText(label)
        canvas.drawText(label, PAGE_WIDTH - MARGIN - labelWidth, y + 10f, orcInfoPaint)

        val orcValuePaint = Paint().apply {
            color = Color.WHITE
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val num = orcamento.numeroOrcamento.ifEmpty { "ORC-0001" }
        val numWidth = orcValuePaint.measureText(num)
        canvas.drawText(num, PAGE_WIDTH - MARGIN - numWidth, y + 32f, orcValuePaint)

        val dateText = "Data: ${DateUtils.formatDate(orcamento.data)}"
        val datePaint = Paint().apply {
            color = Color.WHITE
            textSize = 10f
            isAntiAlias = true
        }
        val dateWidth = datePaint.measureText(dateText)
        canvas.drawText(dateText, PAGE_WIDTH - MARGIN - dateWidth, y + 50f, datePaint)

        return y + headerHeight + 25f
    }

    private fun drawSectionTitle(canvas: Canvas, title: String, startY: Float): Float {
        val sectionPaint = Paint().apply {
            color = Color.parseColor("#1565C0")
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#1565C0")
            strokeWidth = 1.5f
        }

        canvas.drawText(title, MARGIN, startY, sectionPaint)
        val lineY = startY + 4f
        canvas.drawLine(MARGIN, lineY, PAGE_WIDTH - MARGIN, lineY, linePaint)

        return startY + 20f
    }

    private fun drawClientData(canvas: Canvas, cliente: Cliente, startY: Float): Float {
        var y = startY
        val textPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 11f
            isAntiAlias = true
        }
        val labelPaint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
            isAntiAlias = true
        }

        canvas.drawText(cliente.nomeCompleto.ifEmpty { "---" }, MARGIN, y, textPaint)
        y += 16f

        val midX = PAGE_WIDTH / 2f

        if (cliente.cpfCnpj.isNotEmpty()) {
            canvas.drawText("CPF/CNPJ:", MARGIN, y, labelPaint)
            canvas.drawText(cliente.cpfCnpj, MARGIN + 55f, y, textPaint)
        }
        if (cliente.telefone.isNotEmpty()) {
            canvas.drawText("Telefone:", midX, y, labelPaint)
            canvas.drawText(cliente.telefone, midX + 50f, y, textPaint)
        }
        y += 14f

        if (cliente.whatsapp.isNotEmpty()) {
            canvas.drawText("WhatsApp:", MARGIN, y, labelPaint)
            canvas.drawText(cliente.whatsapp, MARGIN + 60f, y, textPaint)
        }
        if (cliente.email.isNotEmpty()) {
            canvas.drawText("E-mail:", midX, y, labelPaint)
            canvas.drawText(cliente.email, midX + 40f, y, textPaint)
        }
        y += 14f

        val endereco = listOfNotNull(
            cliente.endereco.takeIf { it.isNotEmpty() },
            "${cliente.cidade}${if (cliente.estado.isNotEmpty()) " - ${cliente.estado}" else ""}".takeIf { it.isNotEmpty() },
            cliente.cep.takeIf { it.isNotEmpty() }
        ).joinToString(" | ")

        if (endereco.isNotEmpty()) {
            canvas.drawText("Endereço:", MARGIN, y, labelPaint)
            val endTextPaint = TextPaint().apply {
                color = Color.DKGRAY
                textSize = 11f
                isAntiAlias = true
            }
            val endLayout = StaticLayout.Builder.obtain(
                endereco, 0, endereco.length, endTextPaint, (CONTENT_WIDTH - 55f).toInt()
            ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

            canvas.save()
            canvas.translate(MARGIN + 55f, y - 10f)
            endLayout.draw(canvas)
            canvas.restore()
            y += maxOf(endLayout.height + 5f, 18f)
        }

        return y + 4f
    }

    private fun drawEquipmentData(canvas: Canvas, equipamento: Equipamento, startY: Float): Float {
        var y = startY
        val textPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 11f
            isAntiAlias = true
        }
        val labelPaint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
            isAntiAlias = true
        }
        val midX = PAGE_WIDTH / 2f

        canvas.drawText("Tipo:", MARGIN, y, labelPaint)
        canvas.drawText(equipamento.tipoMaquina.descricao, MARGIN + 32f, y, textPaint)
        canvas.drawText("Marca:", midX, y, labelPaint)
        canvas.drawText(equipamento.marca.ifEmpty { "---" }, midX + 38f, y, textPaint)
        y += 14f

        canvas.drawText("Modelo:", MARGIN, y, labelPaint)
        canvas.drawText(equipamento.modelo.ifEmpty { "---" }, MARGIN + 45f, y, textPaint)
        canvas.drawText("Nº Série:", midX, y, labelPaint)
        canvas.drawText(equipamento.numeroSerie.ifEmpty { "---" }, midX + 48f, y, textPaint)
        y += 18f

        y = drawMultilineText(canvas, "Problema Informado:", equipamento.problemaInformado, y, labelPaint)
        y = drawMultilineText(canvas, "Obs. do Técnico:", equipamento.observacoesTecnico, y, labelPaint)

        return y + 4f
    }

    private fun drawServiceData(canvas: Canvas, orcamento: Orcamento, startY: Float): Float {
        var y = startY
        val textPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 11f
            isAntiAlias = true
        }
        val labelPaint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
            isAntiAlias = true
        }

        y = drawMultilineText(canvas, "Descrição do Serviço:", orcamento.descricaoServico, y, labelPaint)

        // Value box
        val boxY = y
        val boxPaint = Paint().apply {
            color = Color.parseColor("#F5F5F5")
            style = Paint.Style.FILL
        }
        canvas.drawRect(MARGIN, boxY, PAGE_WIDTH - MARGIN, boxY + 45f, boxPaint)

        val borderPaint = Paint().apply {
            color = Color.parseColor("#1565C0")
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawRect(MARGIN, boxY, PAGE_WIDTH - MARGIN, boxY + 45f, borderPaint)

        val valueLabelPaint = Paint().apply {
            color = Color.GRAY
            textSize = 11f
            isAntiAlias = true
        }
        canvas.drawText("Valor Total:", MARGIN + 10f, boxY + 18f, valueLabelPaint)

        val valuePaint = Paint().apply {
            color = Color.parseColor("#1565C0")
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val valor = NumberUtils.formatCurrency(orcamento.valorServico)
        val valorWidth = valuePaint.measureText(valor)
        canvas.drawText(valor, PAGE_WIDTH - MARGIN - 10f - valorWidth, boxY + 30f, valuePaint)

        y = boxY + 55f

        canvas.drawText("Forma de Pagamento:", MARGIN, y, labelPaint)
        canvas.drawText(orcamento.formaPagamento.descricao, MARGIN + 115f, y, textPaint)
        y += 18f

        y = drawMultilineText(canvas, "Observações:", orcamento.observacoes, y, labelPaint)

        return y + 10f
    }

    private fun drawMultilineText(canvas: Canvas, label: String, text: String, startY: Float, labelPaint: Paint): Float {
        var y = startY
        if (text.isNotEmpty()) {
            canvas.drawText(label, MARGIN, y, labelPaint)
            y += 13f

            val textPaint = TextPaint().apply {
                color = Color.DKGRAY
                textSize = 11f
                isAntiAlias = true
            }

            val layout = StaticLayout.Builder.obtain(
                text, 0, text.length, textPaint, CONTENT_WIDTH.toInt()
            ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

            canvas.save()
            canvas.translate(MARGIN, y)
            layout.draw(canvas)
            canvas.restore()

            y += layout.height + 8f
        }
        return y
    }

    private fun drawSignatureArea(canvas: Canvas, startY: Float): Float {
        var y = startY + 10f

        if (y < PAGE_HEIGHT - 120f) {
            y = PAGE_HEIGHT - 120f
        } else {
            y += 20f
        }

        val linePaint = Paint().apply {
            color = Color.DKGRAY
            strokeWidth = 1f
        }

        val lineWidth = 300f
        val lineX = (PAGE_WIDTH - lineWidth) / 2
        canvas.drawLine(lineX, y, lineX + lineWidth, y, linePaint)

        val signPaint = Paint().apply {
            color = Color.GRAY
            textSize = 11f
            isAntiAlias = true
        }
        val signText = "Assinatura do Cliente"
        val signWidth = signPaint.measureText(signText)
        canvas.drawText(signText, (PAGE_WIDTH - signWidth) / 2, y + 18f, signPaint)

        return y + 30f
    }

    private fun drawFooter(canvas: Canvas, config: EmpresaConfig?) {
        val footerPaint = Paint().apply {
            color = Color.parseColor("#3C50A0")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, PAGE_HEIGHT - 45f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), footerPaint)

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 9f
            isAntiAlias = true
        }

        val empresa = config?.nomeEmpresa?.ifEmpty { "Tec Manutenção" } ?: "Tec Manutenção"
        val contato = listOfNotNull(
            config?.telefone?.let { "Tel: $it" },
            config?.whatsapp?.let { "WhatsApp: $it" },
            config?.email?.let { "E-mail: $it" }
        ).joinToString(" | ")

        val footerText = "$empresa${if (contato.isNotEmpty()) " | $contato" else ""}"
        val textWidth = textPaint.measureText(footerText)
        canvas.drawText(footerText, (PAGE_WIDTH - textWidth) / 2, PAGE_HEIGHT - 25f, textPaint)

        if (config != null && config.endereco.isNotEmpty()) {
            val addr = config.endereco
            val cityState = listOfNotNull(config.cidade, config.estado).joinToString(" - ")
            val cepText = if (config.cep.isNotEmpty()) "CEP: ${config.cep}" else ""
            val addrText = listOfNotNull(addr, cityState, cepText).joinToString(" | ")
            val addrWidth = textPaint.measureText(addrText)
            canvas.drawText(addrText, (PAGE_WIDTH - addrWidth) / 2, PAGE_HEIGHT - 12f, textPaint)
        }
    }
}
