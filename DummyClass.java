/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE @UMPLE_VERSION@ modeling language!*/



// line 172 "BerkeleySPL.ump"
public class DummyClass
{

  //------------------------
  // MEMBER VARIABLES
  //------------------------

  //DummyClass Attributes
  private String name;

  //------------------------
  // CONSTRUCTOR
  //------------------------

  public DummyClass(String aName)
  {
    name = aName;
  }

  //------------------------
  // INTERFACE
  //------------------------

  public boolean setName(String aName)
  {
    boolean wasSet = false;
    name = aName;
    wasSet = true;
    return wasSet;
  }

  public String getName()
  {
    return name;
  }

  public void delete()
  {}


  public String toString()
  {
    return super.toString() + "["+
            "name" + ":" + getName()+ "]";
  }
}