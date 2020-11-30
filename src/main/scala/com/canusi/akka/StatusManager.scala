package com.canusi.akka

/**
 * Copyright Canusi
 *
 * @author Benjamin Hargrave
 */
case class JobStatus( status: StatusType, warnings: Int, errors: Int )
trait StatusManager{

  import Status._

  private var errorCount = 0
  private var warningCount = 0
  private var status: StatusType = Status.UNDEFINED

  def incrementErrors(): Unit = errorCount +=1
  def incrementWarnings(): Unit = warningCount +=1

  def getStatus(): JobStatus = JobStatus( status, errorCount, warningCount )

  def schedule() = status = UNDEFINED
  def start() = status = RUNNING
  def stop() = status = {
    if( errorCount > 0 ){
      SUCCEEDED_WITH_ERRORS
    } else if ( warningCount > 0 ){
      SUCCEEDED_WITH_WARNINGS
    } else {
      SUCCEEDED
    }
  }

  def finished: Boolean = {
    status.id > 1
  }

  def fail() = status = FAILED

}


sealed trait StatusType{
  val name: String
  val id: Int
}

object Status {

  sealed trait StatusTypeRunning extends StatusType

  sealed trait StatusTypeFinished extends StatusType

  sealed trait StatusTypeQueued extends StatusType

  final object UNDEFINED extends StatusTypeQueued {
    override val name: String = "UNDEFINED"
    override val id: Int = 1
  }
  final object SCHEDULED extends StatusTypeQueued {
    override val name: String = "SCHEDULED"
    override val id: Int = 2
  }

  final object RUNNING extends StatusTypeRunning {
    override val name: String = "RUNNING"
    override val id: Int = 3
  }

  final object SUCCEEDED extends StatusTypeFinished {
    override val name: String = "SUCCEEDED"
    override val id: Int = 4
  }
  final object SUCCEEDED_WITH_ERRORS extends StatusTypeFinished {
    override val name: String = "SUCCEEDED_WITH_ERRORS"
    override val id: Int = 5
  }
  final object SUCCEEDED_WITH_WARNINGS extends StatusTypeFinished {
    override val name: String = "SUCCEEDED_WITH_WARNINGS"
    override val id: Int = 6
  }
  final object FAILED extends StatusTypeFinished {
    override val name: String = "FAILED"
    override val id: Int = 7
  }

}


