package hw3;

import java.util.AbstractSequentialList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Implementation of the list interface based on linked nodes
 * that store multiple items per node.  Rules for adding and removing
 * elements ensure that each node (except possibly the last one)
 * is at least half full.
 */
public class StoutList<E extends Comparable<? super E>> extends AbstractSequentialList<E>
{
  /**
   * Default number of elements that may be stored in each node.
   */
  private static final int DEFAULT_NODESIZE = 4;
  
  /**
   * Number of elements that can be stored in each node.
   */
  private final int nodeSize;
  
  /**
   * Dummy node for head.  It should be private but set to public here only  
   * for grading purpose.  In practice, you should always make the head of a 
   * linked list a private instance variable.  
   */
  public Node head;
  
  /**
   * Dummy node for tail.
   */
  private Node tail;
  
  /**
   * Number of elements in the list.
   */
  private int size;
  
  /**
   * Constructs an empty list with the default node size.
   */
  public StoutList()
  {
    this(DEFAULT_NODESIZE);
  }

  /**
   * Constructs an empty list with the given node size.
   * @param nodeSize number of elements that may be stored in each node, must be 
   *   an even number
   */
  public StoutList(int nodeSize)
  {
    if (nodeSize <= 0 || nodeSize % 2 != 0) { 
    	throw new IllegalArgumentException();
    }
    // dummy nodes
    head = new Node();
    tail = new Node();
    head.next = tail;
    tail.previous = head; 
    this.nodeSize = nodeSize;
  }
  
  /**
   * Constructor for grading only.  Fully implemented. 
   * @param head
   * @param tail
   * @param nodeSize
   * @param size
   */
  public StoutList(Node head, Node tail, int nodeSize, int size)
  {
	  this.head = head; 
	  this.tail = tail; 
	  this.nodeSize = nodeSize; 
	  this.size = size; 
  }
  
  /**
   * size of your StoutList
   */
  @Override
  public int size()
  {
    return size;
  }
  
  @Override
  public boolean add(E item)
  {
	//null exception
	 if(item == null) {
		 throw new NullPointerException();
	 }
	 
	 //if empty node add to front
	 if(size == 0) {
		 Node n = new Node();
		 n.addItem(item);
		 head.next = n; 			//connect head
		 n.next = tail;
		 n.previous = head;
		 tail.previous = n;
	 }else {
		 //if not full, add to end -> tail.previous.count = node before tails index
		 if(tail.previous.count < nodeSize) {
			 tail.previous.addItem(item);
			 
		 }else { //if node full create new node and put item there
			 Node n = new Node();
			 n.addItem(item);
			 
			 Node temp = tail.previous; // one before tail
			 temp.next = n;            
			 n.previous = temp;         
			 n.next = tail;
			 tail.previous = n;
			 }
	 }
	 
	    size++;
		return true;
  }

  
  /**
   * method to add to your list
   */
@Override
public void add(int pos, E item)
  {
	// Check if position is out of bounds
    if(pos < 0 || pos > size) {
        throw new IndexOutOfBoundsException();
    }
      
    // If list is empty, create a new node
    if(head.next == tail) {
       add(item);
    }

    // Find the node and offset at the specified position
    NodeInfo nodeInfo = find(pos);
    Node temp = nodeInfo.node;
    int offset = nodeInfo.offset;

    // If offset is 0
    if (offset == 0) {
        // Check if the previous node has fewer than nodeSize elements
        if (temp.previous.count < nodeSize && temp.previous != head) {
            temp.previous.addItem(item);
            size++;
            return;
        }
        // If temp reaches end, add item to the end
        else if (temp == tail) {
            add(item);
            size++;
            return;
        }
    }

    // If temp has space, add item to it
    if (temp.count < nodeSize) {
        temp.addItem(offset, item);
    }
    // Else, perform a split operation on temp
    else {
        Node newTemp = new Node();
        int half = nodeSize / 2;
        int count = 0;
        while (count < half) {
            newTemp.addItem(temp.data[half]);
            temp.removeItem(half);
            count++;
        }

        Node predecessor = temp.next;

        temp.next = newTemp;
        newTemp.previous = temp;
        newTemp.next = predecessor;
        predecessor.previous = newTemp;

        // If offset is less than or equal to nodeSize/2, add item to temp
        if (offset <= nodeSize / 2) {
            temp.addItem(offset, item);
        }
        // If offset is greater than nodeSize/2, add item to newTemp
        if (offset > nodeSize / 2) {
            newTemp.addItem((offset - nodeSize / 2), item);
        }
    }
    // Increase the size of list, since item has been added
    size++;
}


  @Override
  public E remove(int pos)
  {
	  //if pos is out of bounds throw IndexOutOfBounds
      if (pos < 0 || pos > size)
          throw new IndexOutOfBoundsException();
      // Find the node and offset at the specified position
      NodeInfo info = find(pos);
      Node temp = info.node;
      int offset = info.offset;
      E nodeValue = temp.data[offset];
      
   // Removing the only node in the list
      if (temp.next == tail && temp.count == 1) {
          Node prev = temp.previous;
          prev.next = temp.next;
          temp.next.previous = prev;
          temp = null;
       //Removing from the last node or node has more than half full
      } else if (temp.next == tail || temp.count > nodeSize / 2) {
          temp.removeItem(offset);
      
          //Merging with the next node
      } else {
          temp.removeItem(offset);
          Node successor = temp.next;
          //Merge with next node if it has more than half full
          if (successor.count > nodeSize / 2) {
              temp.addItem(successor.data[0]);
              successor.removeItem(0);
          //Merge with next node if it has half or less elements
          } else if (successor.count <= nodeSize / 2) {
              for (int i = 0; i < successor.count; i++) {
                  temp.addItem(successor.data[i]);
              }
              temp.next = successor.next;
              successor.next.previous = temp;
              successor = null;
          }
      }
   // Update the size of the list and return the removed value
      size--;
      return nodeValue;
  }

  /**
   * Sort all elements in the stout list in the NON-DECREASING order. You may do the following. 
   * Traverse the list and copy its elements into an array, deleting every visited node along 
   * the way.  Then, sort the array by calling the insertionSort() method.  (Note that sorting 
   * efficiency is not a concern for this project.)  Finally, copy all elements from the array 
   * back to the stout list, creating new nodes for storage. After sorting, all nodes but 
   * (possibly) the last one must be full of elements.  
   *  
   * Comparator<E> must have been implemented for calling insertionSort().    
   */

public void sort() {
			
			E[] dataSorter = (E[]) new Comparable[size];

			int arrayCount = 0;
			Node temp = head.next;
			//while there are iterations left to do, move through array 
			while (temp != tail) {
				for (int i = 0; i < temp.count; i++) {
					dataSorter[arrayCount] = temp.data[i];
					arrayCount++;
				}
				temp = temp.next;
			}

			head.next = tail;
			tail.previous = head;
			//use insertion sort on the array
			insertionSort(dataSorter, new SortComparator());
			
			size = 0;
			
			for (int i = 0; i < dataSorter.length; i++) {
				add(dataSorter[i]);
			}

  }
  
  /**
   * Sort all elements in the stout list in the NON-INCREASING order. Call the bubbleSort()
   * method.  After sorting, all but (possibly) the last nodes must be filled with elements.  
   *  
   * Comparable<? super E> must be implemented for calling bubbleSort(). 
   */
  public void sortReverse() 
  {
	  E[] dataSorter = (E[]) new Comparable[size];

		int tempIndex = 0;
		Node temp = head.next;
		while (temp != tail) {
			for (int i = 0; i < temp.count; i++) {
				dataSorter[tempIndex] = temp.data[i];
				tempIndex++;
			}
			temp = temp.next;
		}

		head.next = tail;
		tail.previous = head;
		//use bubble sort on the array
		bubbleSort(dataSorter);
		size = 0;
		for (int i = 0; i < dataSorter.length; i++) {
			add(dataSorter[i]);
		}
  }
  
 
  @Override
  public Iterator<E> iterator()
  {
	  return new StoutListIterator();
  }

  @Override
  public ListIterator<E> listIterator()
  {
    return new StoutListIterator();
  }

  @Override
  public ListIterator<E> listIterator(int index)
  {
    // TODO Auto-generated method stub
    return new StoutListIterator(index);
  }
  
  /**
   * Returns a string representation of this list showing
   * the internal structure of the nodes.
   */
  public String toStringInternal()
  {
    return toStringInternal(null);
  }

  /**
   * Returns a string representation of this list showing the internal
   * structure of the nodes and the position of the iterator.
   *
   * @param iter
   *            an iterator for this list
   */
  public String toStringInternal(ListIterator<E> iter) 
  {
      int count = 0;
      int position = -1;
      if (iter != null) {
          position = iter.nextIndex();
      }

      StringBuilder sb = new StringBuilder();
      sb.append('[');
      Node current = head.next;
      while (current != tail) {
          sb.append('(');
          E data = current.data[0];
          if (data == null) {
              sb.append("-");
          } else {
              if (position == count) {
                  sb.append("| ");
                  position = -1;
              }
              sb.append(data.toString());
              ++count;
          }

          for (int i = 1; i < nodeSize; ++i) {
             sb.append(", ");
              data = current.data[i];
              if (data == null) {
                  sb.append("-");
              } else {
                  if (position == count) {
                      sb.append("| ");
                      position = -1;
                  }
                  sb.append(data.toString());
                  ++count;

                  // iterator at end
                  if (position == size && count == size) {
                      sb.append(" |");
                      position = -1;
                  }
             }
          }
          sb.append(')');
          current = current.next;
          if (current != tail)
              sb.append(", ");
      }
      sb.append("]");
      return sb.toString();
  }


  /**
   * Node type for this list.  Each node holds a maximum
   * of nodeSize elements in an array.  Empty slots
   * are null.
   */
  private class Node{
    /**
     * Array of actual data elements.
     */
	public E[] data = (E[]) new Comparable[nodeSize];
    
    /**
     * Link to next node.
     */
    public Node next;
    
    /**
     * Link to previous node;
     */
    public Node previous;
    
    /**
     * Index of the next available offset in this node, also 
     * equal to the number of elements in this node.
     */
    public int count;

    /**
     * Adds an item to this node at the first available offset.
     * Precondition: count < nodeSize
     * @param item element to be added
     */
    void addItem(E item)
    {
      if (count >= nodeSize)
      {
        return;
      }
      data[count++] = item;
     
      //useful for debugging
      //      System.out.println("Added " + item.toString() + " at index " + count + " to node "  + Arrays.toString(data));
    }
  
    /**
     * Adds an item to this node at the indicated offset, shifting
     * elements to the right as necessary.
     * 
     * Precondition: count < nodeSize
     * @param offset array index at which to put the new element
     * @param item element to be added
     */
    void addItem(int offset, E item)
    {
      if (count >= nodeSize)
      {
    	  return;
      }
      for (int i = count - 1; i >= offset; --i)
      {
        data[i + 1] = data[i];
      }
      ++count;
      data[offset] = item;
      //useful for debugging 
//      System.out.println("Added " + item.toString() + " at index " + offset + " to node: "  + Arrays.toString(data));
    }

    /**
     * Deletes an element from this node at the indicated offset, 
     * shifting elements left as necessary.
     * Precondition: 0 <= offset < count
     * @param offset
     */
    void removeItem(int offset)
    {
      E item = data[offset];
      for (int i = offset + 1; i < nodeSize; ++i)
      {
        data[i - 1] = data[i];
      }
      data[count - 1] = null;
      --count;
    }    
}
  
  
  
 
	//------------------------------------------------------------------------------------------------------------------------------------
 
  private class StoutListIterator implements ListIterator<E>
  {
	// constants you possibly use ...  
	// instance variables ...  
	 
	 // Node cursor;
	  
	  int pos;
	  //int index;
	  int direction;
	  static final int BEHIND = -1;
	  static final int NONE = 0;
	  static final int AHEAD = 1;
	  
	  /**
		 * data structure of iterator in array form
		 */
		public E[] dataList;
	  
    /**
     * Default constructor 
     */
    public StoutListIterator()
    {
    	pos = 0;
		direction = BEHIND;
		dataOrganize();
    }

    /**
     * Constructor finds node at a given position.
     * @param pos
     */
    public StoutListIterator(int pos)
    {
    	this.pos = pos;
		direction = BEHIND;
		dataOrganize();
    }

    @Override
    public boolean hasNext()
    {
    	if (pos >= size)
			return false;
		else
			return true;
    }
	
    @Override
	public E next()
    {
    	if (!hasNext()) {
			throw new NoSuchElementException();
    	}
		direction = AHEAD;
		return dataList[pos++];
    }
    
    

    @Override
    public void remove()
    {
    	if (direction == AHEAD) {
			StoutList.this.remove(pos - 1);
			dataOrganize();
			direction = BEHIND;
			pos--;
			if (pos < 0)
				pos = 0;
		} else if (direction == NONE) {
			StoutList.this.remove(pos);
			dataOrganize();
			direction = BEHIND;
		} else {
			throw new IllegalStateException();
		}
    }
    // Other methods you may want to add or override that could possibly facilitate 
    // other operations, for instance, addition, access to the previous element, etc.
    // 
    // ...
    // 
    /**
	 * Converts the StoutList data into an array format and stores it in dataList.
	 */
	private void dataOrganize() {
		dataList = (E[]) new Comparable[size];

		int tempIndex = 0;
		Node temp = head.next;
		while (temp != tail) {
			for (int i = 0; i < temp.count; i++) {
				dataList[tempIndex] = temp.data[i];
				tempIndex++;
			}
			temp = temp.next;
		}
	}
	
	/**
	 * Checks if there is a previous element in the list.
	 *
	 * @return true if there is a previous element, false otherwise.
	 */
	@Override
	public boolean hasPrevious() {
		if (pos <= 0)
			return false;
		else
			return true;
	}

	@Override
	public E previous() {
		if (!hasPrevious())
			throw new NoSuchElementException();
		direction = NONE;
		pos--;
		return dataList[pos];
	}
	
	/**
	 * Returns the previous element in the list and moves the cursor position backwards.
	 *
	 * @throws NoSuchElementException if there is no previous element in the list.
	 * @return the previous element in the list.
	 */
	@Override
	public int nextIndex() {
		return pos;
	}

	@Override
	public int previousIndex() {
		return pos - 1;
	}
	
	/**
	 * Returns the index of the element before the current cursor position.
	 *
	 * @return the index of the previous element in the list.
	 */
	@Override
	public void set(E e) {
		if (direction == AHEAD) {
			NodeInfo nodeInfo = find(pos - 1);
			nodeInfo.node.data[nodeInfo.offset] = e;
			dataList[pos - 1] = e;
		} else if (direction == NONE) {
			NodeInfo nodeInfo = find(pos);
			nodeInfo.node.data[nodeInfo.offset] = e;
			dataList[pos] = e;
		} else {
			throw new IllegalStateException();
		}
	}
	
	/**
	 * Inserts the specified element at the current cursor position and advances the cursor.
	 *
	 * @param e the element to be added. Must not be null.
	 * @throws NullPointerException if the specified element is null.
	 */
	@Override
	public void add(E e) {
		if (e == null)
			throw new NullPointerException();

		StoutList.this.add(pos, e);
		pos++;
		dataOrganize();
		direction = BEHIND;
	}

 }
  
  

  /**
   * Sort an array arr[] using the insertion sort algorithm in the NON-DECREASING order. 
   * @param arr   array storing elements from the list 
   * @param comp  comparator used in sorting 
   */
  private void insertionSort(E[] arr, Comparator<? super E> comp)
  {
	  for (int i = 1; i < arr.length; i++) {
			E temp = arr[i];
			int j = i - 1;

			while (j >= 0 && comp.compare(arr[j], temp) > 0) {
				arr[j + 1] = arr[j];
				j--;
			}
			arr[j + 1] = temp;
		}
  }
  
  /**
	 * Insertion Sort comparator used when sort is called
	 */

  
  /**
   * Sort arr[] using the bubble sort algorithm in the NON-INCREASING order. For a 
   * description of bubble sort please refer to Section 6.1 in the project description. 
   * You must use the compareTo() method from an implementation of the Comparable 
   * interface by the class E or ? super E. 
   * @param arr  array holding elements from the list
   */
  private void bubbleSort(E[] arr)
  {
	  E temp;
	  
	  for (int i = arr.length - 1; i > 0; i--) {
		  for(int j=0; j < i;j++) {
			  if(arr[j].compareTo(arr[j+1]) <= 0) { 		//<
				  temp = arr[j];
				  arr[j] = arr[j + 1];
				  arr[j+1] = temp;
			  }
		  }
	  }
  } 
 

  
  
  
//------------------------------------------------------------------------------------------------------------------
  
  //MY HELPERS
  
  
  /**
   * generic comparator that compares E's
   * used for sorting
   * @author Reza C
   *
   * @param <E>
   */
 public class SortComparator<E extends Comparable<E>> implements Comparator<E> {
		
		@Override
		public int compare(E a, E b) {
			return a.compareTo(b);
		}

	}
 
 /**
  * Private class representing information about a node along with its offset.
  */
private class NodeInfo {
	 /**
     * The node associated with this NodeInfo.
     */
	public Node node;
	 /**
     * The offset value associated with this NodeInfo.
     */
	public int offset;

	 /**
     * Constructs a new NodeInfo with the specified node and offset.
     *
     * @param node   the node to be associated with this NodeInfo.
     * @param offset the offset value to be associated with this NodeInfo.
     */	
	public NodeInfo(Node node, int offset) {
 			this.node = node;
 			this.offset = offset;
 		}
}



/**
 * Finds a node at a specified position in the list and returns its information.
 *
 * @param pos the position of the node to be found.
 * @return a NodeInfo object containing information about the found node and its offset.
 */ 		
private NodeInfo find(int pos) {
		Node temp = head.next;
		int current = 0;
		while (temp != tail) {
			if (current + temp.count <= pos) {
				current += temp.count;
				temp = temp.next;
				continue;
			}

			
			NodeInfo nodeInfo = new NodeInfo(temp, pos - current);
			return nodeInfo;

		}
		return null;
	}

}
