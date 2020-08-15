package com.example.myapplication.ui.httpserver;

import fi.iki.elonen.NanoHTTPD;

public class HttpServer extends NanoHTTPD {

    public HttpServer(int port) {
        super(port);
    }

        @Override
        public Response serve(IHTTPSession session) {
//            try {
//                session.get
//                for (int i = 0; i < DatabaseSelectUpload.name_.size(); i++) {  //for 循环文件名 小于name的个数
//                    session.parseBody(new HashMap<String, String>());
//                    final String choose = DatabaseSelectUpload.name_.get(i);//获取循环到的文件名
//                    String strDBPath = MyApplication.GetApp().getExternalFilesDir(null) + "/TIS-Smarthome/" + choose + "/" + (choose + ".db3");//数据库地址
//                    FileInputStream fis = new FileInputStream(strDBPath);
//                    return newFixedLengthResponse(Response.Status.OK, "application/octet-stream", fis, fis.available());
//                }
//
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (ResponseException e) {
//                e.printStackTrace();
//            }
            return response404(session, null);
        }


    public Response response404(IHTTPSession session,String url) {
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html>body>");
        builder.append("Sorry,Can't Found" + url + " !");
        builder.append("</body></html>\n");
        return newFixedLengthResponse(builder.toString());
    }
}
