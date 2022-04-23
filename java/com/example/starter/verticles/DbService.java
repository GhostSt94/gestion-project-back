package com.example.starter.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class DbService extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    JsonObject config=new JsonObject()
      .put("db_name","testDB")
      .put("connection_string","mongodb://localhost");
    MongoClient client = MongoClient.create(vertx, config);
    final String COLLECTION="projects";

    EventBus eb=vertx.eventBus();

    eb.consumer("get.project.all.db",msg->{
      JsonObject query=new JsonObject().put("type","project");
      client.find("projects",query,res -> {
        if (res.succeeded()){
          JsonArray arr=new JsonArray();
          res.result().stream().forEach(obj->{
            arr.add(obj);
          });
          msg.reply(arr);
        }else {
          msg.fail(404,"Not found");
        }
      });
    });
    eb.consumer("add.project.db",msg->{
      JsonObject data= (JsonObject) msg.body();
      client.insert("projects",data,res -> {
        if(res.succeeded()){
          msg.reply(res.result());
        }else {
          msg.fail(404,"Error");
        }
      });
    });
    eb.consumer("add.project.file.db",msg->{
      JsonObject message= (JsonObject) msg.body();
      JsonObject query=new JsonObject()
        .put("_id",message.getString("project_id"));
      JsonObject data=new JsonObject()
        .put("$set",new JsonObject()
          .put("file",message.getString("file_name")));

      client.findOneAndUpdate(COLLECTION,query,data,res -> {
        if(res.succeeded()){
          msg.reply(res.result());
        }else {
          msg.fail(404,"Error");
        }
      });
    });
    eb.consumer("get.project.db",msg->{
      String id= msg.body().toString();
      JsonObject query=new JsonObject().put("_id",id);
      client.findOne("projects",query,null,res -> {
        if (res.succeeded()){
          msg.reply(res.result());
        }else {
          msg.fail(404,"Not found");
        }
      });
    });
    eb.consumer("update.project.db",msg->{

      JsonObject data= (JsonObject) msg.body();
      JsonObject setData=new JsonObject()
        .put("$set",data);
      String id= data.getString("_id");
      JsonObject query=new JsonObject().put("_id",id);
      client.findOneAndUpdate("projects",query,setData,res -> {
        if (res.succeeded()){
          msg.reply(res.result());
        }else {
          msg.fail(400,"Error");
        }
      });
    });
    eb.consumer("delete.project.db",msg->{
      String id= msg.body().toString();
      JsonObject query=new JsonObject().put("_id",id);
      client.findOneAndDelete("projects",query,res -> {
        if (res.succeeded()){
          JsonObject query2=new JsonObject()
            .put("type","facture")
              .put("id_project",id);
          client.find("projects",query2,resData->{
            if (resData.succeeded()){
              resData.result().stream().forEach(obj -> {
                eb.send("delete.facture",obj.getString("_id"));
              });
            }
          });
          msg.reply(res.result());
        }else {
          msg.fail(404,"Not found");
        }
      });
    });
    eb.consumer("add.facture.db",msg->{
      JsonObject data= (JsonObject) msg.body();
      client.insert("projects",data,res -> {
        if(res.succeeded()){
          msg.reply(res.result());
        }else {
          msg.fail(404,"Error");
        }
      });
    });
    eb.consumer("add.facture.file.db",msg->{
      JsonObject message= (JsonObject) msg.body();
      JsonObject query=new JsonObject()
        .put("_id",message.getString("facture_id"));
      JsonObject data=new JsonObject()
        .put("$set",new JsonObject()
          .put("file",message.getString("file_name")));

      client.findOneAndUpdate("projects",query,data,res -> {
        if(res.succeeded()){
          msg.reply(res.result());
        }else {
          msg.fail(404,"Error");
        }
      });
    });
    eb.consumer("delete.facture.db",msg->{
      String id= msg.body().toString();
      JsonObject query=new JsonObject()
        .put("_id",id);
      client.findOneAndDelete("projects",query,res -> {
        if(res.succeeded()){
          msg.reply(res.result());
        }else {
          msg.fail(404,"Error");
        }
      });
    });
    eb.consumer("get.facture.all.db",msg->{
      String id=msg.body().toString();
      JsonObject query=new JsonObject()
        .put("type","facture")
        .put("id_project",id);
      client.find("projects",query,res -> {
        if (res.succeeded()){
          JsonArray arr=new JsonArray();
          res.result().stream().forEach(obj->{
            arr.add(obj);
          });
          msg.reply(arr);
        }else {
          msg.fail(404,"Not found");
        }
      });
    });
  }
}
