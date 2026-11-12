import java.io.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

// Represents one hotel room. Serializable allows room objects to be saved in a file.
class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    private int roomNumber;
    private String roomType;
    private double pricePerNight;
    private boolean isBooked;

    public Room(int roomNumber, String roomType, double pricePerNight) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.isBooked = false;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setBooked(boolean booked) {
        isBooked = booked;
    }

    public void displayRoom() {
        System.out.printf("%-12d %-15s Rs.%-12.2f %-10s%n",
                roomNumber, roomType, pricePerNight,
                isBooked ? "Booked" : "Available");
    }
}

// User-defined exception for an attempt to book an already-booked room.
class RoomNotAvailableException extends Exception {
    public RoomNotAvailableException(String message) {
        super(message);
    }
}

public class HotelRoomBookingSystem {
    private static final String FILE_NAME = "rooms.dat";
    private static final ArrayList<Room> rooms = new ArrayList<>();
    private static final Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        loadRooms();

        boolean running = true;
        while (running) {
            System.out.println("\n===== HOTEL ROOM BOOKING SYSTEM =====");
            System.out.println("1. Add a room");
            System.out.println("2. Display all rooms");
            System.out.println("3. Book a room");
            System.out.println("4. Check-out a room");
            System.out.println("5. Exit");
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1:
                    addRoom();
                    break;
                case 2:
                    displayRooms();
                    break;
                case 3:
                    bookRoom();
                    break;
                case 4:
                    checkOutRoom();
                    break;
                case 5:
                    saveRooms();
                    System.out.println("Room data saved. Thank you for using the system!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid menu choice. Please enter a number from 1 to 5.");
            }
        }
        input.close();
    }

    // Reads an integer and handles incompatible input without terminating the program.
    private static int readInt(String message) {
        while (true) {
            System.out.print(message);
            try {
                return input.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a whole number.");
                input.next(); // Discard the invalid token.
            } finally {
                // The newline is consumed by the caller after a successful read.
            }
        }
    }

    private static double readDouble(String message) {
        while (true) {
            System.out.print(message);
            try {
                double value = input.nextDouble();
                if (value <= 0) {
                    System.out.println("Price must be greater than zero.");
                    continue;
                }
                return value;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a numeric price.");
                input.next();
            }
        }
    }

    private static void addRoom() {
        int number = readInt("Enter room number: ");
        input.nextLine(); // Consume the remaining newline.
        if (findRoom(number) != null) {
            System.out.println("A room with this number already exists.");
            return;
        }

        System.out.print("Enter room type (Single/Double/Deluxe): ");
        String type = input.nextLine().trim();
        while (type.isEmpty()) {
            System.out.print("Room type cannot be empty. Enter room type: ");
            type = input.nextLine().trim();
        }

        double price = readDouble("Enter price per night: ");
        input.nextLine(); // Consume the remaining newline.
        rooms.add(new Room(number, type, price));
        System.out.println("Room added successfully.");
    }

    private static void displayRooms() {
        if (rooms.isEmpty()) {
            System.out.println("No rooms have been added yet.");
            return;
        }

        System.out.printf("%-12s %-15s %-15s %-10s%n",
                "Room Number", "Room Type", "Price/Night", "Status");
        System.out.println("----------------------------------------------------------");
        for (Room room : rooms) {
            room.displayRoom();
        }
    }

    private static void bookRoom() {
        int number = readInt("Enter room number to book: ");
        input.nextLine();
        Room room = findRoom(number);

        if (room == null) {
            System.out.println("Room not found.");
            return;
        }

        try {
            if (room.isBooked()) {
                throw new RoomNotAvailableException(
                        "Room " + number + " is already booked.");
            }
            room.setBooked(true);
            System.out.println("Room " + number + " booked successfully.");
        } catch (RoomNotAvailableException e) {
            System.out.println("Booking error: " + e.getMessage());
        }
    }

    private static void checkOutRoom() {
        int number = readInt("Enter room number for check-out: ");
        input.nextLine();
        Room room = findRoom(number);

        if (room == null) {
            System.out.println("Room not found.");
        } else if (!room.isBooked()) {
            System.out.println("This room is not currently booked.");
        } else {
            room.setBooked(false);
            System.out.println("Check-out completed for room " + number + ".");
        }
    }

    private static Room findRoom(int number) {
        for (Room room : rooms) {
            if (room.getRoomNumber() == number) {
                return room;
            }
        }
        return null;
    }

    // Loads previously saved room objects when the program starts.
    @SuppressWarnings("unchecked")
    private static void loadRooms() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("No saved room data found. Starting with an empty list.");
            return;
        }

        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(file))) {
            ArrayList<Room> savedRooms = (ArrayList<Room>) in.readObject();
            rooms.addAll(savedRooms);
            System.out.println(rooms.size() + " room(s) loaded from saved file.");
        } catch (IOException e) {
            System.out.println("Could not read saved room data: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Room class was not found while loading saved data.");
        } catch (ClassCastException e) {
            System.out.println("Saved file has an unexpected data format.");
        }
    }

    // Saves the complete room list when the user exits.
    private static void saveRooms() {
        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(rooms);
        } catch (IOException e) {
            System.out.println("Could not save room data: " + e.getMessage());
        }
    }
}
