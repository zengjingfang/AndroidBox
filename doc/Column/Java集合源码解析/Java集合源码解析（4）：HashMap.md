### 构造


### 扩容

	 final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
        if (oldCap > 0) {
            if (oldCap >= MAXIMUM_CAPACITY) {
				// 如果旧的容量超出最大容量
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
				// 两倍的oldCap 小于最大 oldCap 大于初始容量
                newThr = oldThr << 1; // double threshold 那newThr就扩大两倍
        }
        else if (oldThr > 0) // initial capacity was placed in threshold
            newCap = oldThr;
        else {               // zero initial threshold signifies using defaults
            // 初始值
            newCap = DEFAULT_INITIAL_CAPACITY;
			//
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
            Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        table = newTab;

		
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
				// oldTab都遍历一次
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;
                    if (e.next == null)
						//把第【j】赋值给临时的e
                        newTab[e.hash & (newCap - 1)] = e;
                    else if (e instanceof TreeNode)
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    else { // preserve order
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {
                            next = e.next;
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }


+ 如果旧的容量超出最大容量,则直接将容量设置为Integer.MAX_VALUE
+ 如果旧容量oldCap大于默认容量DEFAULT_INITIAL _CAPACITY，oldCap的两倍小于最大容量，则就扩大两倍， newThr = oldThr << 1。
+ 还有其他扩容策略.....
+ 扩容之后得到新的容器，即：Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
+  newTab[e.hash & (newCap - 1)] = e = oldTab[j];，把数组的节点赋值给到新的容器；
+  处理数组每个元素的链表或者树的元素；


### 