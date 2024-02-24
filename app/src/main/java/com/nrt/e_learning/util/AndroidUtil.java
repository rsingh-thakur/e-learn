package com.nrt.e_learning.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nrt.e_learning.VideoListActivity;
import com.nrt.e_learning.adapters.VideoListAdapter;
import com.nrt.e_learning.model.VideoItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class AndroidUtil {

    public static String getEmailHtmlTemplate(String Transaction_ID, String price, String pdfName) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title> Invoice</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "        }\n" +
                "        .container {\n" +
                "            max-width: 600px;\n" +
                "            margin: auto;\n" +
                "        }\n" +
                "        .header {\n" +
                "            background-color: #3498db; /* Blue color */\n" +
                "            color: #fff; /* White color */\n" +
                "            padding: 20px;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        .invoice-details {\n" +
                "            padding: 20px;\n" +
                "            background-color: #f9f9f9; /* Light gray color */\n" +
                "        }\n" +
                "        .item {\n" +
                "            padding: 10px;\n" +
                "            border-bottom: 1px solid #ddd;\n" +
                "        }\n" +
                "        .footer {\n" +
                "            background-color: #3498db; /* Blue color */\n" +
                "            color: #fff; /* White color */\n" +
                "            padding: 20px;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h2>Course Purchase Invoice</h2>\n" +
                "        </div>\n" +
                "        <div class=\"invoice-details\">\n" +
                "            <p><strong>Transaction ID:</strong> " + Transaction_ID + "</p>\n" +
                "            <p><strong>Payment Status:</strong> Successful</p>\n" +
                "            <p><strong>Amount Paid:</strong> " + price + " </p>\n" +
                "        </div>\n" +
                "        <div class=\"items\">\n" +
                "            <div class=\"item\">\n" +
                "                <p><strong>Course Name:</strong>" + pdfName + "</p>\n" +
                "                <p><strong>Description:</strong> Study Notes for of important questions.</p>\n" +
                "                <p><strong>Enrollment Date:</strong> 22/01/2024 </p>\n" +
                "                <p><strong>Price:</strong> " + price + " </p>\n" +
                "            </div>\n" +
                "            <!-- Add more items as needed -->\n" +
                "        </div>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>Thank you for purchasing the course. If you have any questions, please contact us at basic.erps@gmail.com.</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>\n";
    }


    public static void showToast(Context context, String Message) {
        Toast.makeText(context, Message, Toast.LENGTH_LONG).show();
    }


    public static void convertTextToPdfAndDownload(Context context, String transactionID, String price, String pdfName) {
        try {
            // Get the current date and time
            LocalDateTime currentDateTime = LocalDateTime.now();

            // Format the current date and time
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String formattedDateTime = currentDateTime.format(formatter);

            // Get the path for saving the PDF file
            String pdfFileName = "invoice_" + pdfName + "_" + formattedDateTime + ".pdf";
            String pdfFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + pdfFileName;

            // Create a new PdfDocument instance
            PdfDocument pdfDocument = new PdfDocument();

            // Create a page
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);

            // Get the canvas for drawing
            Canvas canvas = page.getCanvas();

            // Create a Paint object for styling the text
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(12);

            // Define the text content
            String[] lines = {
                    "              Course Purchase Invoice ",
                    "Transaction ID: " + transactionID,
                    "Payment Status: Successful",
                    "Amount Paid: " + price,
                    "Course Name: " + pdfName,
                    "Description: Study Notes for important questions.",
                    "Enrollment Date: " + formattedDateTime,
                    "Price: " + price
            };

            float x = 10; // X-coordinate for drawing text
            float y = 20; // Initial Y-coordinate for drawing text

            // Draw each line of text on the canvas
            for (String line : lines) {
                canvas.drawText(line, x, y, paint);
                y += 20; // Adjust Y-coordinate for next line
            }

            // Finish the page
            pdfDocument.finishPage(page);

            // Save the document to the file
            File file = new File(pdfFilePath);
            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);

            // Close the FileOutputStream and the document
            fos.close();
            pdfDocument.close();

            // Show a toast indicating the PDF is saved
            Toast.makeText(context, "PDF saved to " + pdfFilePath, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("errors", e.getMessage());
            Toast.makeText(context, "Failed to generate PDF ", Toast.LENGTH_SHORT).show();
        }
    }


    public static void fetchVideosFromFirestoreBySearchText(Context context, String searchText, FirebaseFirestore firestore, List<VideoItem> videoItemList, VideoListAdapter videoListAdapter) {
        firestore.collection("videos")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Clear the existing videoItemList
                        videoItemList.clear();

                        // Iterate through the documents and retrieve video data
                        for (DocumentSnapshot document : task.getResult()) {
                            String videoTitle = document.getString("title");
                            String videoUrl = document.getString("url");
                            String thumbnailUrl = document.getString("thumbnail");

                            if (videoTitle.toLowerCase().contains(searchText.toLowerCase())) {
                                // Create Uri objects for video and thumbnail
                                Uri videoUri = Uri.parse(videoUrl);
                                Uri thumbnailUri = null;

                                if (thumbnailUrl != null)
                                    thumbnailUri = Uri.parse(thumbnailUrl);

                                // Create a VideoItem object with both video and thumbnail URIs
                                VideoItem videoItem = new VideoItem(videoUri, videoTitle, thumbnailUri);
                                videoItem.setThumbnailUri(thumbnailUri);

                                // Add the VideoItem to the list
                                videoItemList.add(videoItem);
                            }
                        }
                        videoListAdapter.notifyDataSetChanged();
                    } else {
                        // Handle errors
                        Toast.makeText(context, "Failed to fetch videos from Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
