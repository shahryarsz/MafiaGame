package com.company;

import javax.print.Doc;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class Handler implements Runnable{

    private Socket socket;
    private Server server;
    protected DataOutputStream out;
    private DataInputStream in;
    private String name;
    private Role role;
    private ArrayList<Handler> clients;
    private static ArrayList<String> names = names = new ArrayList<>();
    private boolean canSpeak;
    private boolean canRecieve;
    private boolean isReady;
    private Mode mode;
    private boolean hasRole = false;
    private int votes;
    private Handler votesTo;
    private boolean isAlive;
    private int getShot;

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public Handler(Socket socket , ArrayList<Handler> clients , Server server){
//        this.mode =server.getGame().getMode();
        this.socket = socket;
        this.server = server;
        this.clients = clients;
        canRecieve = false;
        canSpeak = false;

        try {
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true){
                //registering players
                if (!isReady){
                    register();
                    server.increaseReadyPlayers();
                    sendToCLient("Waiting for other players to join..." , this);
                }

                if (server.areAllPlayersReady()) {
                    this.mode = this.server.getGame().getMode();
                    switch (this.mode) {
                        case FIRSTNIGHT:
                            //first night things
                            firstNight();
                            this.server.getGame().setMode(Mode.FREECHAT);
                            this.mode = Mode.FREECHAT;
                        case FREECHAT:
                            //freechat things
                            freeChat();
                            everyOneCanSpeak();
                            this.mode = Mode.VOTING;
                        case VOTING:
                            //voting things
                            voting();
                            resetVoting();
                            this.mode =  Mode.NIGHT;
                        case NIGHT:
                            //night things
                            night();
                            ArrayList<Handler> deadPlayers = nightKills();
                            Handler silentPlayer = silentClient();
                            announceNightThings(deadPlayers , silentPlayer);
                            this.mode = Mode.FREECHAT;
                    }
                }

            }
        }catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public  boolean night() throws IOException, InterruptedException {
        sendToCLient("[GOD]:Its night . Please close your eyes." , this);
//        silentEveryOne();


        //mafia thing
        if (this.role instanceof Citizen && this.isAlive){
            sendToCLient("[GOD]:Waking up Mafia team..." , this);
            synchronized (this){
                this.wait();
//                return true;
            }
        }

        if (this.role instanceof Mafia && this.isAlive){
            mafiaAct();
            synchronized (this) {
                for (Handler c : clients){
                    if (c.isAlive) {
                        synchronized (c) {
                            c.notify();
                        }
                    }
                }
            }
        }

        //lecter thing
        if (gameHasRole("Doctor lecter")) {
            if (!(this.role instanceof DoctorLecter) && this.isAlive){
                sendToCLient("[GOD]:Waking up Doctor Lecter..." , this);
                synchronized (this){
                    this.wait();
                }
            }
            if (this.role instanceof DoctorLecter){
                if (this.isAlive) {
                    lecterAct();
                }
                else {
                    Thread.sleep(7000);
                }
                synchronized (this) {
                    for (Handler c : clients){
                        if (c.isAlive){
                            synchronized (c) {
                                c.notify();
                            }
                        }
                    }
                }

            }
        }

        //doctor thing
        if (!(this.role instanceof Doctor) && this.isAlive){
            sendToCLient("[GOD]:Waking up Doctor..." , this);
            synchronized (this){
                this.wait();
//                return true;
            }
        }
        if (this.role instanceof Doctor){
            if (this.isAlive) {
                doctorAct();
            } else {
                Thread.sleep(7000);
            }
            synchronized (this) {
                for (Handler c : clients){
                    if (c.isAlive){
                        synchronized (c) {
                            c.notify();
                        }
                    }
                }
            }
        }


        //detective thing
        if (!(this.role instanceof Detective) && this.isAlive){
            sendToCLient("[GOD]:Waking up Detective..." , this);
            synchronized (this){
                this.wait();
            }
        }
        if (this.role instanceof Detective){
            if (this.isAlive) {
                detectiveAct();
            } else {
                Thread.sleep(7000);
            }
            synchronized (this) {
                for (Handler c : clients){
                    if ( c.isAlive){
                        synchronized (c) {
                            c.notify();
                        }
                    }
                }
            }
        }

        //sniper thing
        if (!(this.role instanceof Sniper) && this.isAlive){
            sendToCLient("[GOD]:Waking up Sniper..." , this);
            synchronized (this){
                this.wait();
            }
        }
        if (this.role instanceof Sniper) {
            if (this.isAlive)
                sniperAct();
            else {
                Thread.sleep(7000);
            }
            synchronized (this) {
                for (Handler c : clients){
                    if (c.isAlive){
                        synchronized (c) {
                            c.notify();
                        }
                    }
                }
            }
        }

        //Psychologist things
        if (gameHasRole("Psychologist")){
            if (!(this.role instanceof Psychologist) && this.isAlive){
                sendToCLient("[GOD]:Waking up Psychologist..." , this);
                synchronized (this){
                    this.wait();
                }
            }
            if (this.role instanceof Psychologist){
                if (this.isAlive)
                    psychologistAct();
                else
                    Thread.sleep(7000);

                synchronized (this) {
                    for (Handler c : clients){
                        if (c.isAlive){
                            synchronized (c) {
                                c.notify();
                            }
                        }
                    }
                }
            }

        }

        //badkooft things
        if (gameHasRole("Badkooft")){
            if (!(this.role instanceof Sniper) && this.isAlive){
                sendToCLient("[GOD]:Waking up Badkooft..." , this);
                synchronized (this){
                    this.wait();
                }
            }
            if (this.role instanceof Badkooft){
                if (this.isAlive)
                    badkooftAct();
                else
                    Thread.sleep(7000);

                synchronized (this) {
                    for (Handler c : clients){
                        if ( c.isAlive){
                            synchronized (c) {
                                c.notify();
                            }
                        }
                    }
                }
            }
        }



//        synchronized (this) {
//            for (Handler c : clients){
//                if ( c.isAlive){
//                    synchronized (c) {
//                        c.notify();
//                    }
//                }
//            }
//        }
        sendToCLient("DEAD DONE" , this);
        sendToCLient("SILENT DONE" , this);
        sendToCLient("FUCK YOU" , this);
//
//
//        Handler badKooft = findPlayerByRole("Badkooft");
//        if (((Badkooft)badKooft.role)!=null && ((Badkooft)badKooft.role).isHasAsked()){
//            badkooftAnnounce();
//        }
//
//        if (this.role instanceof Badkooft){
//            ((Badkooft) this.role).setHasAsked(false);
//        }

        return false;


    }

    public void badkooftAnnounce() throws IOException {
        ArrayList<Handler> deadPlayers = new ArrayList<>();
        for (Handler client : clients){
            if (!client.isAlive){
                deadPlayers.add(client);
            }
        }
        Collections.shuffle(deadPlayers);
        sendToCLient("[GOD]:This roles aren't in the game anymore:" , this);
        int i=1;
        for (Handler client : deadPlayers){
            sendToCLient("  "+i+")"+client.getRole().name , this);
            i++;
        }
    }

    public void announceNightThings(ArrayList<Handler> deadPlayers , Handler silentPlayer) throws IOException {
        sendToCLient("\n[GOD]:Last night this things happened:\n" , this);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (deadPlayers.size()>0){
            sendToCLient("  -[GOD]:This players died last night:" , this);
            int i=1;
            for (Handler client : deadPlayers){
                sendToCLient("      "+i+")"+client.name , this);
                i++;
            }
        }else {
            sendToCLient("[GOD]:Nobody died last night." , this);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (silentPlayer!=null){
            sendToCLient("  -[GOD]:And " + silentPlayer.getName() + " is silent." , this);
        }
    }

    public synchronized ArrayList <Handler> nightKills() throws IOException, InterruptedException {
        ArrayList<Handler> deadPlayers = new ArrayList<>();
        for (Handler client : clients){
            if (client.isAlive && client.getShot>0){
                client.isAlive = false;
                deadPlayers.add(client);
//                sendToCLient("[GOD]:You have been killed last night , if you want to see the rest of the game type 1 else 2." , client);
//                String choice =client.in.readUTF();
//                client.canRecieve = choice.equals("1");
            }
//            else {
//                synchronized (this){
//                    wait();
//                }
//            }
        }
        return deadPlayers;
    }

    public synchronized Handler silentClient(){
        for (Handler client : clients){
            if (client.isAlive && !client.canSpeak){
                return client;
            }
        }
        return null;
    }

    public boolean gameHasRole(String roleName){
        for (Handler client : clients){
            if (client.role.name.equals(roleName))
                return true;

        }
        return false;
    }

    public void silentEveryOne(){
        for (Handler client : clients){
            if (client.isAlive){
                client.canRecieve = false;
                client.canSpeak = false;
            }
        }
    }

    public void everyOneCanSpeak(){
        for (Handler client : clients){
            if (client.isAlive){
                client.canSpeak = true;
            }
        }
    }

    public void badkooftAct()throws IOException{
        if (((Badkooft)this.role).getCanAsk()!=0){
            sendToCLient("[GOD]:if you want to ask what happened at night type 1." , this);
            String choice = in.readUTF();
            if (choice.equals("1")){
                ((Badkooft) this.role).setCanAsk();
                ((Badkooft) this.role).setHasAsked(true);
            }
        }else {
            sendToCLient("[GOD]:You cant ask about night things anymore!" , this);
            try {
                Thread.sleep(7000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void psychologistAct() throws IOException{
        if (((Psychologist)this.role).getCanSilent()!=0){
            sendToCLient("[GOD]:You can silent a player if you want to type 1." , this);
            String choice = in.readUTF();
            if (choice.equals("1")){
                while (true){
                    sendToCLient("[GOD]:Which player you want to silent?" , this);
                    String name = in.readUTF();
                    Handler player = findPlayerByName(name);
                    if (player.isAlive && player!=null){
                        player.canSpeak = false;
                        ((Psychologist) this.role).setCanSilent();
                        break;
                    }else {
                        sendToCLient("[GOD]:Wrong input." , this);
                    }
                }
            }
        }else {
            sendToCLient("[GOD]:You cant silent anymore!" , this);
            try {
                Thread.sleep(7000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void sniperAct() throws IOException{
        if (((Sniper)this.role).getHasShot()!= 0){
            sendToCLient("[GOD]:You have one shot , if you want to take your shot type 1 else type 2." , this);
            String choice =  in.readUTF();
            if (choice.equals("1")){
                while (true){
                    sendToCLient("[GOD]:Which player you want to shoot?" , this);
                    String name = in.readUTF();
                    Handler player = findPlayerByName(name);
                    if (player!=null && player.isAlive){
                        if (player.role instanceof Badkooft ){
                            if (((Badkooft) player.role).getHasArmor()>0) {
                                ((Badkooft) player.role).setHasArmor(0);
                                ((Sniper) this.role).setHasShot(0);
                                break;
                            }
                        }
                        shot(player);
                        ((Sniper) this.role).setHasShot(0);
                        break;
                    }else {
                        sendToCLient("[GOD]:Wrong input." , this);
                    }
                }
            }
        }else {
            sendToCLient("[GOD]:You cant shoot anymore!" , this);
            try {
                Thread.sleep(7000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void detectiveAct() throws IOException {
        sendToCLient("[GOD]:Choose a player and I tell you if its mafia or not!" , this);
        String choice;
        while (true){
            choice = in.readUTF();
            if (findPlayerByName(choice).role instanceof Citizen || findPlayerByName(choice).role instanceof GodFather){
                sendToCLient("[GOD]:This player is not mafia." , this);
                break;
            }else if (findPlayerByName(choice).role instanceof Mafia){
                sendToCLient("[GOD]:This player is mafia." , this);
                break;
            }else if (findPlayerByName(choice) == null){
                sendToCLient("[GOD]:Wrong input." , this);
            }
        }
    }

    public void lecterAct() throws IOException {
        if (((DoctorLecter)this.role).getCanHeal()!=0){
            sendToCLient("[GOD]:If you want to heal one of mafias type 1 else type 2." , this);
            String choice = in.readUTF();
            if (choice.equals("1")){
                while (true) {
                    sendToCLient("[GOD]:Which mafia you want to heal?", this);
                    String name = in.readUTF();
                    Handler player = findPlayerByName(name);
                    if (player != null && player.role.isMafia && player.isAlive) {
                        player.getShot--;
                        ((DoctorLecter) this.role).setCanHeal(0);
                        break;
                    }else {
                        sendToCLient("[GOD]:Wrong input!" , this);
                    }
                }
            }
        }else{
            sendToCLient("[GOD]:You cant heal anymore" , this);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void doctorAct()throws IOException{
        if (((Doctor)this.role).getCanHeal()!=0){
            sendToCLient("[GOD]:If you want to heal one of the players type 1 else type 2." , this);
            String choice = in.readUTF();
            if (choice.equals("1")){
                while (true) {
                    sendToCLient("[GOD]:Which player you want to heal?", this);
                    String name = in.readUTF();
                    Handler player = findPlayerByName(name);
                    if (player != null && player.isAlive) {
                        player.getShot--;
                        ((Doctor) this.role).setCanHeal(0);
                        break;
                    }else {
                        sendToCLient("[GOD]:Wrong input!" , this);
                    }
                }
            }
        }else {
            sendToCLient("[GOD]:You cant heal anymore" , this);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public  void mafiaAct() throws IOException {
        //Citizens sleeping
        for (Handler client : clients){
            if (client.role instanceof Citizen && client.isAlive){
                client.canSpeak = false;
                client.canRecieve = false;
            }
        }
        //announcing them and creating their chatroom for only 30 seconds
        sendToCLient("[GOD]:You have 30 seconds for consulting!" , this);
        long start = System.currentTimeMillis();
        long end = start + 2 * 1000;
        String line;
        int bytes = 0;
        while (System.currentTimeMillis()< end) {
            bytes = in.available();
            if (bytes>0) {
                line = in.readUTF();
                System.out.println(line);
                if (this.canSpeak)
                    sendToAll(this.name, line);
                else {
                    sendToCLient("[GOD]:You are dead!" , this);
                }
            }
        }
        sendToCLient("[GOD]:TIMES UP!" , this);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String mafiasChoice;
        setMafiasPower();
        if (this.role instanceof Mafia){
            if (((Mafia) this.role).hasPower){
                sendToCLient("[GOD]:You should type a player name to shoot." , this);
                while (true) {
                    mafiasChoice = in.readUTF();
                    Handler client = findPlayerByName(mafiasChoice);
                    if (client!=null){
                        if (client.role instanceof Badkooft){
                            if (((Badkooft) client.role).getHasArmor()>0) {
                                ((Badkooft) client.role).setHasArmor(0);
                                break;
                            }
                        }
                        shot(client);
                        break;
                    }else {
                        sendToCLient("Wrong input!" , this);
                    }
                }
            } else {
                synchronized (this){
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        for (Handler client : clients){
            if (client.role instanceof Citizen && client.isAlive){
                client.canSpeak = true;
                client.canRecieve = true;
            }
        }

    }

    //giving the killing power to the head mafia
    public synchronized void setMafiasPower(){
        Handler godFather = findPlayerByRole("God father");
        Handler doctorLecter = findPlayerByRole("Doctor lecter");
        Handler simpleMafia = findPlayerByRole("Havij Mafia");
        if (godFather.isAlive && godFather!=null){
            ((Mafia)godFather.role).hasPower = true;
        }else if (doctorLecter.isAlive && doctorLecter!=null){
            ((Mafia)doctorLecter.role).hasPower = true;
        }else if (simpleMafia.isAlive && simpleMafia!=null){
            ((Mafia)simpleMafia.role).hasPower = true;
        }
    }

    //shooting a player a player
    public void shot(Handler client) throws IOException {
        client.getShot++;
    }

    //finding player by name
    public Handler findPlayerByName(String name){
        for (Handler client : clients){
            if (client.name.equals(name) && client.isAlive){
                    return client;
            }
        }
        return null;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public Handler getVotesTo() {
        return votesTo;
    }

    public void setVotesTo(Handler votesTo) {
        this.votesTo = votesTo;
    }

    public void startVoting() throws IOException {
        long start = System.currentTimeMillis();
        long end = start + 2*1000;
        String line;
        int bytes = 0;
        while (System.currentTimeMillis()<end){
            bytes = in.available();
            if (bytes>0) {
                line = in.readUTF();
                System.out.println(line);
                if (submitVote(line)) {
                    sendToCLient("[GOD]:You successfully voted!" , this);
                    //changing vote ---------> remember to done
                } else {
                    sendToCLient("[GOD]:Wrong input!Try again." , this);
                }
            }
        }
    }

    public boolean voting()throws IOException{
        sendToCLient("[GOD]:Voting time!you have 30 seconds to vote a player who seems to be mafia!" , this);
        sendToCLient("[GOD]:This is list of players :\n" , this);
        //showing players list
        showPlayersList();
        sendToCLient("-for voting a player please just type the players name!" , this);
        //starting voting
        startVoting();
        sendToCLient("\n[GOD]:VOTING TIME'S UP!\n" , this);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //showing the results
        showVotes();
        //final voting stuff
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Handler client = whoToLeave();
//        String choice="";
        if (client==null)
            sendToCLient("[GOD]:Nobody leaves the game." , this);
        else{
            if (!(this.role instanceof Mayor) && this.isAlive){
                sendToCLient("[GOD]:Waiting for the Mayor..." , this);
                synchronized (this){
                    try {
                        this.wait();
                        return true;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (this.role instanceof Mayor && this.isAlive){
                mayorAct(client);
            }else if (this.role instanceof Mayor && !this.isAlive){
                client.isAlive=false;
                client.canSpeak = false;
                sendToCLient("[GOD]:Mayor voted to kill you. if you wanna see the rest of game type 1" , client);
                String choice1 =client.in.readUTF();
                client.canRecieve = choice1.equals("1");
                sendToAll("GOD" , client.getName() + " died.");
            }
            synchronized (this) {
                for (Handler c : clients){
                    if (!(c.role instanceof Mayor) && c.isAlive){
                        synchronized (c) {
                            c.notify();
                        }
                    }
                }
            }
        }
        return false;
    }

    //act of mayor after the voting
    public void mayorAct(Handler client){
        String choice;
        try {
            sendToCLient("[GOD]:if you are agree to kill " + client.getName()+" type (yes) if not type (no)" , this);
            while (true) {
                choice = in.readUTF();
                if (choice.equals("yes")) {
                    client.isAlive = false;
                    client.canSpeak = false;
                    sendToCLient("[GOD]:Mayor voted to kill you. if you wanna see the rest of the game type 1." , client);
                    String choice1 = client.in.readUTF();
                    client.canRecieve = choice1.equals("1");
                    sendToAll("GOD" , client.getName() + " died.");
                    break;
                } else if (choice.equals("no")) {
                    sendToCLient("[GOD]:Mayor voted to save you." , client);
                    sendToAll("GOD" ,"Mayor saved " + client.getName() );
                    break;
                } else {
                    sendToCLient("Wrong input" , this);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //resting voting results
    public void resetVoting(){
        for (Handler client : clients){
            client.votes=0;
            client.votesTo = null;
        }
    }

    //find out who should leave the game by voting
    public Handler whoToLeave(){
        int max = findMaxVote();
        int count=0;
        for (Handler client:clients){
            if (client.getVotes()==max)
                count++;
        }
        if (count==1)
            return findPlayerByVote(max);
        else
            return null;
    }

    //finding a player by number of votes
    public Handler findPlayerByVote(int max){
        for (Handler client : clients){
            if (client.getVotes()==max)
                return client;
        }
        return null;
    }

    //finding max number of votes
    public int findMaxVote(){
        int max=0;
        for (Handler client : clients){
            if (client.getVotes()>max)
                max = client.getVotes();
        }
        return max;
    }

    //showing voting results
    public void showVotes()throws IOException{
        int i=1;
        for (Handler c : clients){
            if (c.isAlive){
                sendToCLient(i +")" + c.getName() +" has " + c.getVotes() +" votes." , this);
                i++;
                if (c.getVotes()>0) {
                    sendToCLient("- These are the voters:" , this);
                    for (Handler voter : clients) {
                        if (voter.getVotesTo().getName().equals(c.getName())) {
                            sendToCLient("   -" + voter.getName() , this);
                        }
                    }
                }
            }
        }
    }

    //submitting a vote
    public boolean submitVote(String name){
        if (this.name.equals(name))
            return false;
        for (Handler client : clients){
            if (client.name.equals(name) && client.isAlive){
                this.votesTo = client;
                client.votes++;
                return true;
            }
        }
        return false;
    }

    //showing alive players list
    public void showPlayersList() throws IOException{
        int i = 1;
        for (Handler client : clients){
            if (client.isAlive) {
                sendToCLient(i + ")" + client.getName() , this);
                i++;
            }
        }
    }

    //free chat things
    public void freeChat()throws IOException {
        long start = System.currentTimeMillis();
        long end = start + 10 * 1000;
        String line;
        int bytes = 0;
        while (System.currentTimeMillis()< end) {
            bytes = in.available();
            if (bytes>0) {
                line = in.readUTF();
                System.out.println(line);
                if (line.equals("quit")) {
                    quit();
                    return;
                }
                if (this.canSpeak)
                    sendToAll(this.name, line);
                else {
                    sendToCLient("[GOD]:You can't chat!" , this);
                }
            }
        }
        sendToCLient("\n[GOD]:DAY CHAT TIMES UP\n" , this);
    }

    //first night things
    public void firstNight() throws IOException {
        //telling everybody for starting first night --------> do it better
        sendToCLient("[GOD]:Hey everyone this is first night." , this);
        //telling mafia  ---------> do it better
        showMafias();
        //telling citizens -------> do it better
        if (this.getRole() instanceof Citizen){
            sendToCLient("[GOD]:Your role is " + this.getRole().name , this);
            if (this.getRole() instanceof Mayor){
                Handler doc = findPlayerByRole("Doctor");
                sendToCLient("[GOD]:" + doc.name + " is Doctor!" , this);
            }
        }
    }

    //showing mafias to each other
    public void showMafias() throws IOException {
        int i = 1;
        if (this.role instanceof Mafia){
            if (server.getGame().getMafiaCount()==1) {
                sendToCLient("You are " + this.getRole().name , this);
            } else {
                sendToCLient("You are " + this.getRole().name + " and the other Mafias are:" , this);
                for (Handler client : clients) {
                    if (client.role.isMafia && !client.name.equals(this.name)) {
                        sendToCLient(i + ")" + client.name + " is " + client.getRole().name , this);
                        i++;
                    }
                }
            }
        }
    }

    //finding a player by its role
    public Handler findPlayerByRole(String role){
        for (Handler client : clients){
            if (client.getRole().name.equals(role)){
                return client;
            }
        }
        return null;
    }


    //for quiting from the chat ------------> has very very things to do
    public void quit() throws IOException{
        sendToCLient("You left the game! if you want to see the rest of chat press 1 else 2!" , this);
        while (true) {
            String command = in.readUTF();
            if (!command.isBlank() && command.equals("1")) {
                this.canRecieve = true;
                break;
            } else if (!command.isBlank() && command.equals("2")) {
                this.canRecieve = false;
                break;
            }
            sendToCLient("Wrong input." , this);
        }
        this.canSpeak = false;
        sendToAll("GOD" , this.name +" left the game!");
    }

    //for registering to the chat room
    public void register() throws IOException{
        while (true){
            sendToCLient("Enter your username:" , this);
            String name = in.readUTF();
            if (!names.contains(name) && !name.isBlank()){
                names.add(name);
                this.name = name;
                break;
            }else if (names.contains(name)){
                sendToCLient("This user is in the game!Please choose another name." , this);
            }
        }
        while (true){
            sendToCLient("Type (ready) to join the game!" , this);
            String command = in.readUTF();
            if (!command.isBlank() && command.equals("ready")) {
                this.canSpeak = true;
                this.canRecieve = true;
                this.isAlive = true;
                sendToAll("GOD" , name + " joined the game!");
                this.isReady = true;
                int index = clients.indexOf(this);
                this.role =server.getRoles().get(index);
                break;
            }else {
                sendToCLient("Wrong input." , this);
            }
        }
    }

    //sending a text to everyone
    public void sendToAll(String sender , String msg){
        for (Handler client : clients){
            try {
                if (client.canRecieve) {
                    client.out.writeUTF("["+ sender +  "] : " + msg);
                    client.out.flush();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void sendToCLient(String msg , Handler client) throws IOException {
        client.out.writeUTF(msg);
        client.out.flush();
    }



}
