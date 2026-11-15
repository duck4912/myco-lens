package com.example.projekt;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class MushroomClassifier {

    private static final String MODEL_FILE = "mycolens_model.tflite";
    private static final String LABELS_FILE = "labels.txt";
    private static final int IMAGE_SIZE = 224;

    private final Interpreter interpreter;
    private final List<String> labels;

    public static class ClassificationResult {
        public final String label;
        public final float confidence;

        public ClassificationResult(String label, float confidence) {
            this.label = label;
            this.confidence = confidence;
        }
    }

    public MushroomClassifier(Context context) throws IOException {
        MappedByteBuffer modelBuffer = loadModelFile(context);
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);
        this.interpreter = new Interpreter(modelBuffer, options);
        this.labels = loadLabels(context);
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> loadLabels(Context context) throws IOException {
        List<String> labelList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open(LABELS_FILE)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    labelList.add(line.trim());
                }
            }
        }
        return labelList;
    }

    public ClassificationResult classify(Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);
        ByteBuffer inputBuffer = convertBitmapToByteBuffer(resizedBitmap);

        float[][] outputProbabilities = new float[1][labels.size()];
        interpreter.run(inputBuffer, outputProbabilities);

        int maxIndex = 0;
        float maxProb = 0f;
        for (int i = 0; i < labels.size(); i++) {
            if (outputProbabilities[0][i] > maxProb) {
                maxProb = outputProbabilities[0][i];
                maxIndex = i;
            }
        }

        return new ClassificationResult(labels.get(maxIndex), maxProb);
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * IMAGE_SIZE * IMAGE_SIZE * 3);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[IMAGE_SIZE * IMAGE_SIZE];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int pixel = 0;
        for (int i = 0; i < IMAGE_SIZE; ++i) {
            for (int j = 0; j < IMAGE_SIZE; ++j) {
                final int val = intValues[pixel++];
                byteBuffer.putFloat(((val >> 16) & 0xFF));
                byteBuffer.putFloat(((val >> 8) & 0xFF));
                byteBuffer.putFloat((val & 0xFF));
            }
        }
        return byteBuffer;
    }

    public void close() {
        interpreter.close();
    }
}