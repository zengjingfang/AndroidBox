###  关键点

+  默认大小：10
+  扩容倍数：1.5倍
+  扩展方式：原始数据copy到扩容后的数组中
+  删:将要删除的元素后面所有的元素往前1位复制，就替换掉了要删除的，最后一位置换为null
+  改、查，直接按照索引来

### 源码解析
		
   	private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

    public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

	public boolean add(E e) {
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        elementData[size++] = e;
        return true;
    }
	private void ensureCapacityInternal(int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
			// 这个方法特别关键，如果默认的构造为，则保证elementData最小size为10
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        ensureExplicitCapacity(minCapacity);
    }

    private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        // overflow-conscious code
        // 当前的容量不够，需要扩展
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }

	 private void grow(int minCapacity) {
        // overflow-conscious code
		// 旧容量
        int oldCapacity = elementData.length;
		// 新容量  newCapacity = oldCapacity*1.5倍
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
 			//最小不能小过minCapacity
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
			//最大就直接最大的
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
		// 构建出新的elementData，将原始数据copy过来
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

Arrays.java

    public static <T> T[] copyOf(T[] original, int newLength) {
        return (T[]) copyOf(original, newLength, original.getClass());
    }

	public static <T,U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {

		// 判断是否都是Object类型，是则直接new，否则要去重新构建
        T[] copy = ((Object)newType == (Object)Object[].class)
            ? (T[]) new Object[newLength]
            : (T[]) Array.newInstance(newType.getComponentType(), newLength);
		
		// native方法，将 original > copy
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        // 返回copy
        return copy;
    }
	
ArrayList.java
	
    public void add(int index, E element) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));

        ensureCapacityInternal(size + 1);  // Increments modCount!!
        System.arraycopy(elementData, index, elementData, index + 1,
                         size - index);
        elementData[index] = element;
        size++;
    }

	public boolean remove(Object o) {
		// 循环遍历elementData每个元素，直到找到传入的obj
        if (o == null) {
            for (int index = 0; index < size; index++)
                if (elementData[index] == null) {
                    fastRemove(index);
                    return true;
                }
        } else {
            for (int index = 0; index < size; index++)
                if (o.equals(elementData[index])) {
                    fastRemove(index);
                    return true;
                }
        }
        return false;
    }

    private void fastRemove(int index) {
        modCount++;
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null; // clear to let GC do its work
    }

### 改

    public E set(int index, E element) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));

        E oldValue = (E) elementData[index];
        elementData[index] = element;
        return oldValue;
    }

### 查


    public E get(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));

        return (E) elementData[index];
    }

![](https://docs.google.com/drawings/d/e/2PACX-1vSLMF8qPiympINu73Ga1F0vQQX3YkyRhxqygOm9vxzXpKUj0S3efAkCXpU_8CZ2ga_ZUpSD3c-ItTtU/pub?w=418&h=237)