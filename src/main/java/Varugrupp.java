import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class Varugrupp implements Iterable<Vara> {

    private final String name;
    private final Set<Vara> varor = new LinkedHashSet<>();

    public Varugrupp(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean add(Vara vara) {
        if (vara == null) {
            throw new IllegalArgumentException("Product must not be null");
        }
        return varor.add(vara);
    }

    public boolean remove(Vara vara) {
        return varor.remove(vara);
    }

    public boolean contains(Vara vara) {
        return varor.contains(vara);
    }

    public int size() {
        return varor.size();
    }

    @Override
    public Iterator<Vara> iterator() {
        return Collections.unmodifiableSet(varor).iterator();
    }
}
