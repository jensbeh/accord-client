package util;

import de.uniks.stp.model.User;

import java.util.Comparator;

public class SortUser implements Comparator<User> {
    // Used for sorting in ascending order of
    // name
    @Override
    public int compare(User a, User b) {
        return a.getName().toLowerCase().compareTo(b.getName().toLowerCase());
    }
}