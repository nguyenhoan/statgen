package test;

public class TestArrayList<E> {
    Object[] elementData;
    
    /**
     * Returns the element at the specified position in this list.
     *
     * @param  index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E get(int index) {
        rangeCheck(index);
        return elementData(index);
    }
    
    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException();
    }
    
    @SuppressWarnings("unchecked")
    E elementData(int index) {
        return (E) elementData[index];
    }
    
}
