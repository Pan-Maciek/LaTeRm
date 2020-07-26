package reactive.design.data

trait Modifiable[A] {

  /**
    * Checks whether data has been modified since last peek.
    */
  def isModified(): Boolean

  /**
    * After calling this method isModified should return false.
    * Returns view of hidden data.
    */
  def peek(): A
}
