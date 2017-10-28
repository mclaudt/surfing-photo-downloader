import java.io.File

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.nio.JpegWriter

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


object Run extends App{

//  val id = 27592
//  val id = 27327
//  val id = 27432
  val id = 27537



//Максимальное увеличение 15, на 16 все сваливается в дефолтный вид
  val z = 15

  val l = z*2-1

  val widthSource = 400

  val heightSource = 266

  val dx = widthSource/(2.0*z)

  val dy = heightSource/(2.0*z)

  val widthPiece = 200

  val heightPiece = 133

  //x0 и y0 можно полагать равным нулю для простоты, так как то. что влияет, это сумма x0 и x1 (y0 и y1) плюс увеличение.

  private def url(id:Int,x1:Int,y1:Int) =
      "http://media.surfingsilvercoast.com/members/image.php?" +
        s"id=${id}&" +
        s"x0=0&" +
        s"y0=0&" +
        s"x1=${f"${(x1 - 1) * dx}%1.3f"}&" +
        s"y1=${f"${(y1 - 1) * dy}%1.3f"}&" +
        s"z=${z}&" +
        s"width=${2*widthPiece}&" +
        s"height=${2*heightPiece}"


  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  def getImage(id:Int, x1:Int, y1:Int): Image =  {

    val resF = Http().singleRequest(HttpRequest(uri =url(id,x1,y1)))
        .flatMap(
        _.entity.toStrict(10.seconds)).map(
      e=>e.data.toArray
    ).map(a=>Image(a).trimLeft(widthPiece).trimBottom(heightPiece))
   val res = Await.ready(resF, Duration.Inf).value.get.get
    println(s"$x1, $y1 ok")
    res
  }

  implicit val writer = JpegWriter()

  val result= (for {
    i<- 1 to l
    j<- 1 to l
  } yield (i,j)).map(t => (t, getImage(id, t._1, t._2))).foldLeft(Image.filled(l * widthPiece, l * heightPiece))(
    (img, pair) =>img.overlay(pair._2, (pair._1._1-1) * widthPiece, (pair._1._2-1) * heightPiece)).output(new File(s"surf_${id}.jpg"))

println("done")



}
