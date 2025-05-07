import express from "express";
import mgov from "mgov-uploader";

const app = express();
const port = 3000;

app.use(express.static("public"));

app.get("/", (req, res) => {
  res.send("RCS File Upload Server");
});

app.get("/api/data", async (req, res) => {
  try {
    const obj = {
      domain: "http://example.com",
      clientId: "your_client_id",
      clientPwd: "your_password",
      brandId: "",
      filepath: "/path/to/your/file.jpg",
    };

    const response = await mgov.handleFileUpload(
        obj.domain,
        obj.clientId,
        obj.clientPwd,
        obj.brandId,
        obj.filepath
    );
    res.json(response);
  } catch (err) {
    console.error(err);
    res.status(500).json({message: "서버 오류 발생", error: err.message});
  }
});

app.listen(port, () => {
  console.log(`서버가 ${port}포트번호에서 실행 중입니다..`);
});
