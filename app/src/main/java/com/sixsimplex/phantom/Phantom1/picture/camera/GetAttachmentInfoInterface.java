package com.sixsimplex.phantom.Phantom1.picture.camera;


import java.io.File;

public interface GetAttachmentInfoInterface {

    void getCaptureImageInfo(File file, String fileType);

    void deletePicture(int position);
}
