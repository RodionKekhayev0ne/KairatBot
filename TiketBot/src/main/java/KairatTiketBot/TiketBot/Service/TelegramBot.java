package KairatTiketBot.TiketBot.Service;

import KairatTiketBot.TiketBot.Admin.AdminStatus;
import KairatTiketBot.TiketBot.config.BotConfig;

import KairatTiketBot.TiketBot.repo.CountRepo;
import KairatTiketBot.TiketBot.repo.StudentRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.URL;


@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;
    Integer ticketCount = 0;
    int i = 1;
    final String filePath = "src/main/resources/data.json";


    private AdminStatus status = AdminStatus.DEFAULT;

    private StudentRepo studentRepo;
    private CountRepo countRepo;

    public TelegramBot(BotConfig config,StudentRepo studentRepo,CountRepo countRepo) {
        this.studentRepo = studentRepo;
        this.countRepo = countRepo;
        this.config = config;




    }

    @Override
    public String getBotUsername() {
        return config.getName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()){
            String textMessage = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (status){
                case PASS:

                    if (checkPass(update.getMessage().getText())){
                        status = AdminStatus.TICKETUPDT;
                        sendMenu(update.getMessage().getChatId(), "Отправте билеты");
                        break;
                    }else if(update.getMessage().getText().equals("/exit")){
                        status = AdminStatus.EXIT;
                    }else {
                        sendMenu(chatId,"Неверный пароль :(");
                        break;
                    }
                case EXIT:
                    sendMenu(chatId,"Выход из админки");
                    status = AdminStatus.DEFAULT;
                    break;


            }



            switch (textMessage){
                case "/start":
                    showMenu(chatId,update.getMessage().getChat().getFirstName());
                    try {
                        addStudent(chatId,update.getMessage().getChat().getFirstName());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "/ticket":
                    startTicketTaker(chatId);
                    break;
                case "/addtickets":
                    status = AdminStatus.PASS;
                    sendMenu(chatId,"Вход в админку - для выхода напишите /exit");
                    sendMenu(chatId,"Введите пароль");
                    break;
                default:


            }
        } if (update.hasMessage() && update.getMessage().hasDocument()){

            switch (status) {
                case TICKETUPDT:
                    try {
                        uploadTickets(update.getMessage().getDocument().getFileName(),update.getMessage().getDocument().getFileId());
                        sendMenu(update.getMessage().getChatId(),"Готово");
                        status = AdminStatus.EXIT;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


            }



        }
    }

    private void showMenu(long chatId, String name){

        String answer = "Здравствуйте " + name + "\n" + "Чтобы получить билет на матч клуба 'Кайрат' напишите: /ticket";


        sendMenu(chatId,answer);
    }



    private void startTicketTaker(long chatId){

        TicketCount ticketCount = countRepo.findById(1).get();
        if (ticketCount.getCount().intValue() < 1){
            try {
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText("Просим прощения в данный момент билеты закончились");
            }catch (Exception ex){
                System.out.println("Проверка кол-ва билетов барахлит");
            }

        }else {
            Student student = studentRepo.findById(chatId).get();
            if (student.getTicketCount() < 2) {
                String answer = "Ваш билет";


                Integer num = countRepo.findById(1).get().getCount();

                String path = "C:\\Users\\L0veL1v3\\Desktop\\tickets\\Ticket_" + num + ".pdf";

                TicketCount count = countRepo.findById(1).get();
                count.setCount(num - 1);
                countRepo.save(count);

                sendTicket(chatId, path, answer);
                studentRepo.delete(student);
                student.setTicketCount(student.getTicketCount().intValue() + 1);
                studentRepo.save(student);
            } else {
                sendMenu(chatId, "Вам предоставленно максимальное число билетов '2 билета'");
            }
        }
    }



    private void sendMenu(long chatId, String text){



        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);


        try {
            execute(message);
        }catch (TelegramApiException ex){
            ex.printStackTrace();
        }
    }

    private void sendTicket(long chatId, String path, String text){

        if(studentRepo.findById(chatId) != null) {

            Integer count = countRepo.findById(1).get().getCount();
            if (count < 1){
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText("Просим прощения в данный момент билеты закончились");
            }else {
                InputFile ticket = new InputFile(path);
                File file = new File(path);
                ticket.setMedia(file);

                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText(text);
                SendDocument document = new SendDocument();
                document.setChatId(String.valueOf(chatId));
                document.setDocument(ticket);

                try {
                    execute(message);
                    execute(document);
                    file.delete();
                } catch (TelegramApiException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }


    private void uploadTickets(String fileName, String fileId) throws Exception {



        for (Student student : studentRepo.findAll()){
            student = studentRepo.findById(student.getId()).get();
            student.setTicketCount(0);
            studentRepo.save(student);
        }
        File file = new File("C:\\Users\\L0veL1v3\\Desktop\\tickets\\source\\");
        file.delete();

        URL url = new URL("https://api.telegram.org/bot" + getBotToken() + "/getFile?file_id=" + fileId);

        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        String getFileResponse = reader.readLine();

        JSONObject result = new JSONObject(getFileResponse);
        JSONObject path = result.getJSONObject("result");
        String filePath = path.getString("file_path");

        File localFile = new File("C:\\Users\\L0veL1v3\\Desktop\\tickets\\source\\" + fileName);
        InputStream inputStream = new URL("https://api.telegram.org/file/bot" + getBotToken() + "/" + filePath).openStream();

        FileUtils.copyInputStreamToFile(inputStream,localFile);

        separateTickets(localFile.getPath());




    }

    private void separateTickets(String path) throws Exception {
        log.info("SEPARATING FROM " + path);
        String inputFilePath = path; // Замените на путь к вашему входному PDF-файлу
        String outputDirectory = "C:\\Users\\L0veL1v3\\Desktop\\tickets\\"; // Папка, в которую будут сохранены отдельные страницы

        try {
            PDDocument document = PDDocument.load(new File(inputFilePath));

            int pageNum = 1;
            for (PDPage page : document.getPages()) {
                PDDocument singlePageDocument = new PDDocument();
                singlePageDocument.addPage(page);
                String outputFileName = outputDirectory + "Ticket_" + pageNum + ".pdf";
                singlePageDocument.save(outputFileName);
                singlePageDocument.close();
                pageNum++;

            }

            document.close();
            TicketCount count = countRepo.findById(1).get();
            count.setCount(pageNum-1);
            countRepo.save(count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    final String password = "Ognaviastralis06";

    protected boolean checkPass(String pass){
        if (pass.equals(password)){
            return true;
        }else {
            return false;
        }
    }


    private void addStudent(Long chatId,String userName) {

        if (studentRepo.findById(chatId).isPresent()){
            log.info("User with chat id - " + chatId + " using tg-bot name - " + userName);
        }else {
            Student student = new Student();
            student.setName(userName);
            student.setId(chatId);
            student.setTicketCount(0);

            studentRepo.save(student);

            log.info("Created new user - " + userName);
        }
    }




}










