package db;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportGenerator {

    public static final String CREATED_BY = "LUSOKE ATKA YUSRA";
    public static final String SYSTEM_NAME = "HARDWARE MANAGEMENT SYSTEM";
    public static final String LOGO_PATH = "logo.png";

    public static void generate(String title, String sql, File outFile) throws Exception {
        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        PdfWriter.getInstance(doc, new FileOutputStream(outFile));
        doc.open();

        Font systemFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.DARK_GRAY);
        Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{1f, 5f});

        PdfPCell logoCell = new PdfPCell();
logoCell.setBorder(PdfPCell.NO_BORDER);
try {
    Image img = null;
    java.net.URL url = ReportGenerator.class.getResource("/logo.png");
    if (url != null) {
        img = Image.getInstance(url);
    } else {
        File logo = new File(LOGO_PATH);
        if (logo.exists()) {
            img = Image.getInstance(logo.getAbsolutePath());
        }
    }
    if (img != null) {
        img.scaleToFit(70, 70);
        logoCell.addElement(img);
    }
} catch (Exception ignored) {
}
header.addCell(logoCell);

        PdfPCell textCell = new PdfPCell();
        textCell.setBorder(PdfPCell.NO_BORDER);
        textCell.addElement(new Paragraph(SYSTEM_NAME, systemFont));
        textCell.addElement(new Paragraph(title, titleFont));
        textCell.addElement(new Paragraph("Created by: " + CREATED_BY + "   |   Generated: "
                + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()), smallFont));
        header.addCell(textCell);
        doc.add(header);
        doc.add(new Paragraph(" "));

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            PdfPTable table = new PdfPTable(cols);
            table.setWidthPercentage(100);

            for (int i = 1; i <= cols; i++) {
                PdfPCell c = new PdfPCell(new Phrase(md.getColumnLabel(i), headFont));
                c.setBackgroundColor(new BaseColor(40, 70, 120));
                c.setPadding(5);
                table.addCell(c);
            }
            table.setHeaderRows(1);

            int rows = 0;
            while (rs.next()) {
                for (int i = 1; i <= cols; i++) {
                    String v = rs.getString(i);
                    PdfPCell c = new PdfPCell(new Phrase(v == null ? "" : v, cellFont));
                    c.setPadding(4);
                    table.addCell(c);
                }
                rows++;
            }

            if (rows == 0) {
                doc.add(new Paragraph("No records found for this report.", cellFont));
            } else {
                doc.add(table);
                doc.add(new Paragraph(" "));
                doc.add(new Paragraph("Total records: " + rows, smallFont));
            }
        } finally {
            doc.close();
        }
    }
}
