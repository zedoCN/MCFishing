package com.zedo.mcfishing;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.stage.Screen;

import java.util.Timer;
import java.util.TimerTask;

public class HelloController {
    @FXML
    private Canvas canvas;
    @FXML
    private Label stateLabel;
    @FXML
    private Label stopLabel;
    @FXML
    private Label fishLabel;

    @FXML
    private Label labelRunCount;
    @FXML
    private Label labelRunTime;
    @FXML
    private Label labelAverageTime;
    @FXML
    private CheckBox checkBox;


    int runCount = 0;
    long runTime = 0;
    @FXML
    private Slider sliderX;
    @FXML
    private Slider sliderY;

    Robot robot = new Robot();

    public static Timer timer = new Timer();
    Rectangle2D screenRectangle = Screen.getPrimary().getBounds();
    double width = screenRectangle.getWidth();
    double height = screenRectangle.getHeight();
    int[] stopCounts = new int[400];
    int[] fishCounts = new int[400];

    int state = 5;//0抛竿 1等待 2检查 3收杆 4等待 5空闲
    long time = 0;//计时

    int fishingCount = 0;//钓鱼阈值

    {

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    WritableImage writableImage = new WritableImage(400, 200);
                    robot.getScreenCapture(writableImage, width * (sliderX.getValue() / 100) - 200, height * (sliderY.getValue() / 100) - 100, 400, 200);
                    PixelReader pixelReader = writableImage.getPixelReader();
                    GraphicsContext g = canvas.getGraphicsContext2D();
                    PixelWriter pixelWriter = g.getPixelWriter();
                    pixelWriter.setPixels(0, 0, 400, 200, pixelReader, 0, 0);
                    int stopCount = 0;
                    int fishCount = 0;

                    boolean isShow = checkBox.isSelected();
                    for (int y = 0; y < 200; y++) {
                        for (int x = 0; x < 400; x++) {
                            Color color = pixelReader.getColor(x, y);


                            if (compareColor(color, Color.GREEN) < 0.5) {
                                if (isShow)
                                    pixelWriter.setColor(x, y, Color.YELLOW);

                                fishCount++;
                            } else if (compareColor(color, Color.BLUE) < 1) {
                                stopCount++;
                                if (isShow)
                                    pixelWriter.setColor(x, y, Color.BLUE);
                            } else {
                                if (isShow)
                                    pixelWriter.setColor(x, y, Color.BLACK);
                            }
                        }
                    }
                    g.setLineWidth(2);

                    g.setStroke(Color.YELLOWGREEN);
                    g.strokeLine(0, 200 - ((fishingCount / 3000.) * 200), 400, 200 - ((fishingCount / 3000.) * 200));


                    g.setStroke(Color.DARKRED);
                    g.strokeLine(0, 200 - ((20000 / 80000.) * 200), 400, 200 - ((20000 / 80000.) * 200));


                    for (int x = 0; x < 399; x++) {


                        g.setStroke(Color.RED);
                        g.strokeLine(x, 200 - ((stopCounts[x] / 80000.) * 200), x + 1, 200 - ((stopCounts[x + 1] / 80000.) * 200));

                        g.setStroke(Color.GREEN);
                        g.strokeLine(x, 200 - ((fishCounts[x] / 3000.) * 200), x + 1, 200 - ((fishCounts[x + 1] / 3000.) * 200));


                        stopCounts[x] = stopCounts[x + 1];
                        fishCounts[x] = fishCounts[x + 1];
                    }
                    stopCounts[399] = stopCount;
                    fishCounts[399] = fishCount;


                    //pixelWriter.setColor();

                    stopLabel.setText("停止值: " + stopCount);
                    fishLabel.setText("钓鱼值: " + fishCount);

                    if (runTime != 0) {
                        if (runCount != 0)
                            labelAverageTime.setText((int) ((System.currentTimeMillis() - runTime) / 10. / runCount) / 100. + "s");
                    }
                    if (runTime != 0)
                        labelRunTime.setText((int) ((System.currentTimeMillis() - runTime) / 10.) / 100. + "s");
                    if (state == 0) {

                        robot.mouseClick(MouseButton.SECONDARY);
                        stateLabel.setText("状态: 抛竿");
                        time = System.currentTimeMillis();
                        fishingCount = Integer.MAX_VALUE;
                        state = 1;
                    } else if (state == 1) {


                        if (time + 2000 < System.currentTimeMillis()) {

                            fishingCount *= 0.5;
                            state = 2;
                        }
                        stateLabel.setText("状态: 抛竿 等待 " + (2000 - (System.currentTimeMillis() - time)) + "ms");
                        if (time + 1000 < System.currentTimeMillis()) {
                            stateLabel.setText("状态: 抛竿 采集 " + (2000 - (System.currentTimeMillis() - time)) + "ms");
                            if (fishingCount > fishCount)
                                fishingCount = fishCount;
                            if (stopCount < 20000) {
                                state = 5;
                            }
                        }


                    } else if (state == 2) {
                        if (fishingCount <= 0) {
                            state = 4;
                        }
                        stateLabel.setText("状态: 检测 阈值 " + fishingCount);
                        if (fishCount < fishingCount) {
                            state = 3;
                        }
                        if (stopCount < 20000) {
                            state = 5;
                        }
                    } else if (state == 3) {
                        runCount++;

                        labelRunCount.setText(String.valueOf(runCount));


                        stateLabel.setText("状态: 上钩");
                        robot.mouseClick(MouseButton.SECONDARY);
                        time = System.currentTimeMillis();
                        state = 4;
                    } else if (state == 4) {
                        stateLabel.setText("状态: 上钩 等待 " + (1000 - (System.currentTimeMillis() - time)) + "ms");
                        if (time + 1000 < System.currentTimeMillis()) {
                            state = 0;
                        }
                        if (stopCount < 20000) {
                            state = 5;
                        }
                    } else if (state == 5) {
                        runTime = 0;
                        stateLabel.setText("状态: 闲置");
                    } else if (state == 6) {
                        stateLabel.setText("状态: 即将开始");
                    }


                    //System.out.println("greenCount"+greenCount);
                    /* */

                    //System.out.println(greenCount);

                    //timer.cancel();
                });

            }
        }, 1000, 66);
    }

    @FXML
    protected void startButtonClick() {
        Platform.runLater(() -> {

            new Thread(() -> {
                state = 6;
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                state = 0;
                runTime = System.currentTimeMillis();
            }).start();

        });


    }

    public double compareColor(Color color, Color color2) {
        return (Math.abs(color2.getRed() - color.getRed()) + Math.abs(color2.getGreen() - color.getGreen()) + Math.abs(color2.getBlue() - color.getBlue()));
    }
}