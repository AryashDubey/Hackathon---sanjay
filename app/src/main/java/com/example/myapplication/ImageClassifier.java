package com.example.myapplication;
import android.app.Activity;
import android.graphics.Bitmap;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.image.ops.Rot90Op;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class ImageClassifier {
    private static final float PROBABILITY_MEAN =0.0f ;
    private static final float PROBABILITY_STD =255.0f ;
    private static final float IMAGE_MEAN =0.0f ;
    private static final float IMAGE_STD = 1.0f;
    private static final int MAX_SIZE = 1;
    private final Interpreter tensorInterpreter;
    private final int imageResizeX;
    private final int imageResizeY;
    private final  List<String> labels = null;
    private TensorImage inputImageBuffer;
    private final TensorBuffer probabilityImageBuffer;
    private final TensorProcessor probabilityProcessor;


    public ImageClassifier (Activity activity) throws IOException {
        MappedByteBuffer classifiermodel =FileUtil.loadMappedFile(activity,"model.tflite");
        labels.add("benign");
        labels.add("malignant");
        tensorInterpreter = new Interpreter(classifiermodel,null);
        int ImageTensorIndex = 0;
        int probabilityTensorIndex=0;
        int[] inputimageShape = tensorInterpreter.getInputTensor(ImageTensorIndex).shape();
        DataType inputdatatype = tensorInterpreter.getInputTensor(probabilityTensorIndex).dataType();
        int[] outputImageShape = tensorInterpreter.getOutputTensor(probabilityTensorIndex).shape();
        DataType outputDataType = tensorInterpreter.getOutputTensor(probabilityTensorIndex).dataType();
        imageResizeX = inputimageShape[1];
        imageResizeY = inputimageShape[2];
        inputImageBuffer = new TensorImage(inputdatatype);
        probabilityImageBuffer = TensorBuffer.createFixedSize(outputImageShape, outputDataType);
        probabilityProcessor = new TensorProcessor.Builder().add(new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD))
                .build();

    }
    public List<Recognition> recognizeimage(final Bitmap bitmap,final int sensorOrientation) {
        List<Recognition> recognitions = new ArrayList<>();
        inputImageBuffer = loadimage(bitmap,sensorOrientation);
        tensorInterpreter.run(inputImageBuffer.getBuffer(),probabilityImageBuffer.getBuffer().rewind());
        Map<String, Float> labelledProbability = new TensorLabel(labels,
                probabilityProcessor.process(probabilityImageBuffer)).getMapWithFloatValue();
        for (Map.Entry<String, Float> entry : labelledProbability.entrySet()) {
            recognitions.add(new Recognition(entry.getKey(), entry.getValue()));
        }
        Collections.sort(recognitions);
        recognitions.subList(0,MAX_SIZE>recognitions.size()? recognitions.size(): MAX_SIZE).clear();
        return recognitions;
    }

    private TensorImage loadimage(Bitmap bitmap, int sensorOrientation) {
        inputImageBuffer.load(bitmap);
        int noofrotation = sensorOrientation/90;
        int cropSize = Math.min(bitmap.getWidth(),bitmap.getHeight());
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                .add(new ResizeOp(imageResizeX, imageResizeY, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                .add(new Rot90Op(noofrotation))
                .add(new NormalizeOp(IMAGE_MEAN, IMAGE_STD))
                .build();
        return imageProcessor.process(inputImageBuffer);
    }

    class Recognition implements Comparable{
        private String name;
        private float confidence;

        public Recognition(){

        }
        public Recognition(String name, float confidence) {
            this.name = name;
            this.confidence = confidence;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public float getConfidence() {
            return confidence;
        }

        public void setConfidence(float confidence) {
            this.confidence = confidence;
        }

        @Override
        public String toString() {
            return "Recognition{" +
                    "name='" + name + '\'' +
                    ", confidence=" + confidence +
                    '}';
        }

        @Override
        public int compareTo(Object o) {
            return Float.compare(((Recognition) o).confidence, this.confidence);
        }
    }


}
