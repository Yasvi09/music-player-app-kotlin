import android.os.Parcel
import android.os.Parcelable

data class MusicFiles(
    var path: String? = null,
    var title: String? = null,
    var artist: String? = null,
    var album: String? = null,
    var duration: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(path)
        parcel.writeString(title)
        parcel.writeString(artist)
        parcel.writeString(album)
        parcel.writeString(duration)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<MusicFiles> {
        override fun createFromParcel(parcel: Parcel): MusicFiles {
            return MusicFiles(parcel)
        }

        override fun newArray(size: Int): Array<MusicFiles?> {
            return arrayOfNulls(size)
        }
    }
}
