package lottery;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LotteryController {
    public Label label;
    public Button button;
    private AtomicInteger status;
    private ArrayList<String> guestArray;

    private enum Status {
        NoData(0), Waiting(1), Rolling(2);

        private final int value;
        Status(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    };

    public LotteryController() {
        status = new AtomicInteger(Status.NoData.getValue());
        guestArray = GuestList.getEntireGuestList();
        status.set(Status.Waiting.value);
    }

    public void startStop(ActionEvent actionEvent) {
        if (status.get() == Status.NoData.getValue()) {
            // No guest data. Prompt to get a guest list file.
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose Guest List");
            File file = fileChooser.showOpenDialog(label.getScene().getWindow());
            if (file != null) {
                try {
                    Scanner scanner = new Scanner(file);
                    guestArray = new ArrayList<String>(60);
                    while (scanner.hasNextLine()) {
                        guestArray.add(scanner.nextLine());
                    }
                    // Switch status to waiting.
                    status.set(Status.Waiting.value);
                    button.setText("开始");
                }
                catch (Exception e) {
                }
            }
            status.set(Status.Waiting.value);
            button.setText("开始");
        }
        else if (status.compareAndSet(Status.Rolling.getValue(), Status.Waiting.value)) {
            button.setText("开始");
            // Remove the selected person from the entire array
            guestArray.remove(label.getText());
        }
        else {
            status.set(Status.Rolling.value);
            button.setText("停！");
            RandomNameSelector selector = new RandomNameSelector(guestArray);
            label.textProperty().bind(selector.messageProperty());
            new Thread(selector).start();
        }
    }

    class RandomNameSelector extends Task<Void> {
        private ArrayList<String> guests;
        private Random randomGenerator = new Random();

        RandomNameSelector(ArrayList<String> list) {
            guests = list;
        }

        @Override
        public Void call() throws Exception {
            while (status.get() == Status.Rolling.getValue()) {
                int index = randomGenerator.nextInt(guests.size());
                updateMessage(guests.get(index));
                try {
                    Thread.sleep(10);
                }
                catch (InterruptedException e) {
                    break;
                }
            }

            return null;
        }
    }
}

