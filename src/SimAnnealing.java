import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Random;
import java.util.stream.Collectors;

public class SimAnnealing {
    private static final int NUM_TASKS = 10;
    private static final int MAX_TEMP = 100;
    private static final int MIN_TEMP = 1;
    private static final double COOLING_RATE = 0.002;

    private static int[] startOrder = {1,2,3,4,5,6,7};
    private static int[][] dependencies = {
            {1,3}, {3,5}, {2,4}, {4,6}, {5,7}, {6,7}
    };
    private static int[] durations =  {10, 5, 8, 6, 12, 7, 9,};
    private static int[] resources = {2, 3, 2, 4, 2, 3, 4,};

    private static int maxResources;
    private static List<Task> tasks;

    public static void main(String[] args) throws IOException {
        maxResources = TaskReader.readMaxResources("plan0.txt");
        tasks = TaskReader.readTasksFromFile("plan0.txt");


        System.out.println(tasks.toString());
        System.out.println(calculateDuration(tasks));
       /* tasks = generateNewOrder(tasks);
        System.out.println(tasks.toString());
        System.out.println(calculateDuration(tasks));*/

        //int[] order = generateNewOrder(startOrder);
        double temp = MAX_TEMP;
        int it = 1;
        while (temp > MIN_TEMP) {
            int currentDuration = calculateDuration(tasks);
            var newTasks = generateNewOrder(tasks);

            int newDuration = calculateDuration(newTasks);

            if (acceptanceProbability(currentDuration, newDuration, temp) >= Math.random()) {
                tasks = newTasks;
            }

            temp =  (temp * (1 - COOLING_RATE));
            //temp = (temp - 0.0001);
            //temp = (int) (MAX_TEMP * 0.1 / it);
            it++;
        }

        System.out.println("Optimal task order: " + tasks.toString());
        System.out.println("Project duration: " + calculateDuration(tasks));
        System.out.println("Iterations: "+ it);;


        /*System.out.println(Arrays.toString(startOrder));
        System.out.println(calculateDuration(startOrder));*/
    }

    public static double acceptanceProbability(int currentDuration, int newDuration,double temp){
        if (newDuration<currentDuration) {
            return 1.0;
        }

        return Math.exp((double) -( newDuration- currentDuration)/temp);
    }

    // доделать генерацию и протестить
    public static int[] generateNewOrder(int[] order){
        int [] newOrder = order.clone();
        swap(newOrder,new Random().nextInt(newOrder.length), new Random().nextInt(newOrder.length));
        while (!isValid(newOrder)){
            swap(newOrder,new Random().nextInt(newOrder.length), new Random().nextInt(newOrder.length));
        }
        //System.out.println("Вышел");
        return newOrder;
    }

    public static List<Task> generateNewOrder (List<Task> tasks) {
        List<Task> newTasks = new ArrayList<>(tasks);
        Collections.swap(newTasks,new Random().nextInt(newTasks.size()), new Random().nextInt(newTasks.size()));
        while (!isValid(newTasks)){
            Collections.swap(newTasks,new Random().nextInt(newTasks.size()), new Random().nextInt(newTasks.size()));
        }
        return newTasks;
    }

    public static void swap(int[] arr, int i, int j){
        int t = arr[i];
        arr[i] = arr[j];
        arr[j] = t;
    }

    public static int calculateDuration(int [] order){
        int currentBusyResources = 0;
        int projectDuration = 0;
        //List<Task> scheduledTasks = new ArrayList<>();

        for (int task : order) {
            if (currentBusyResources + resources[task-1] <= maxResources) {
                currentBusyResources += resources[task-1];
                projectDuration = Math.max(projectDuration, durations[task-1]);
               // scheduledTasks.add(task);
            } else {
                currentBusyResources = resources[task-1];
                projectDuration += durations[task-1];
               // scheduledTasks.add(task);
            }
        }
        return projectDuration;
    }

    public static int calculateDuration (List<Task> tasks){
        int currentBusyResources = 0;
        int projectDuration = 0;
        //List<Task> scheduledTasks = new ArrayList<>();

        for (var task : tasks) {
            if (currentBusyResources + task.resource <= maxResources) {
                currentBusyResources += task.resource;
                projectDuration = Math.max(projectDuration, task.duration);
                // scheduledTasks.add(task);
            } else {
                currentBusyResources = task.resource;
                projectDuration += task.duration;
                // scheduledTasks.add(task);
            }
        }
        return projectDuration;
    }

    public static boolean isValid(int[] order) {

        List<Integer> orderList = Arrays.stream(order).boxed().toList();
        for (int[] dep : dependencies) {
            int index1 = orderList.indexOf(dep[0]);
            int index2 = orderList.indexOf(dep[1]);
            if (index1>index2){

                return false;
            }

        }
        return true;
    }
    public static boolean isValid (List<Task> tasks){
        for (var task : tasks){
            int id1 = tasks.indexOf(task);
            for (var follow : task.followers){
                Task taskFind = tasks.stream()
                        .filter(t -> t.id == follow)
                        .findFirst()
                        .orElse(null);
                if (id1 > tasks.indexOf(taskFind)){
                    return false;
                }
             }
        }
        return true;
    }
}


class Task {
    int id;
    int duration;
    int resource;
    int numFollowers;
    List<Integer> followers;
    public Task(int id, int duration, int resource, int numFollowers, List<Integer> followers){
        this.id = id;
        this.duration= duration;
        this.resource = resource;
        this.numFollowers =numFollowers;
        this.followers = followers;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", duration=" + duration +
                ", resource=" + resource +
                ", numFollowers=" + numFollowers +
                ", followers=" + followers +
                "}\n";
    }
}

class TaskReader {
    public static List<Task> readTasksFromFile (String filename) throws IOException {
            List<Task> tasks = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                String line = br.readLine(); // читаем первую строку
                int numTasks = Integer.parseInt(line.split(" ")[0]); // получаем количество задач
                int numResources = Integer.parseInt(line.split(" ")[1]); // получаем количество ресурсов
                line = br.readLine(); // читаем вторую строку
                int maxResourceLimit = Integer.parseInt(line); // получаем максимальное ограничение по ресурсу
                for (int i = 0; i < numTasks; i++) {
                    line = br.readLine(); // читаем следующую строку
                    String[] tokens = line.split("\\s+"); // разбиваем строку по пробелам
                    int id = i+1;
                    int duration = Integer.parseInt(tokens[0]);
                    int resource = Integer.parseInt(tokens[1]);
                    int numFollowers = Integer.parseInt(tokens[2]);
                    List<Integer> followers = new ArrayList<>();
                    for (int j = 3; j < tokens.length; j++) {
                        followers.add(Integer.parseInt(tokens[j]));
                    }
                    tasks.add(new Task(id, duration, resource, numFollowers, followers));
                }
            }
            return tasks;
    }
    public static int readMaxResources (String filename) throws IOException {
        int maxResourceLimit;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))){
            String line = br.readLine(); // читаем первую строку
            line = br.readLine(); // читаем вторую строку
            maxResourceLimit = Integer.parseInt(line); // получаем максимальное ограничение по ресурсу
        }
        return maxResourceLimit;
    }
}