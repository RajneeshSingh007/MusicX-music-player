package com.rks.musicx.data.network.VagModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by Coolalien on 6/28/2016.
 */

/*
 * ©2017 Rajneesh Singh
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class Vag {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("art")
    @Expose
    private Art art;
    @SerializedName("mus")
    @Expose
    private List<Mu> mus = new ArrayList<Mu>();
    @SerializedName("badwords")
    @Expose
    private Boolean badwords;
    @SerializedName("extra")
    @Expose
    private List<Alb> alb = new ArrayList<Alb>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Art getArt() {
        return art;
    }

    public void setArt(Art art) {
        this.art = art;
    }

    public List<Mu> getMus() {
        return mus;
    }

    public void setMus(List<Mu> mus) {
        this.mus = mus;
    }

    public Boolean getBadwords() {
        return badwords;
    }

    public void setBadwords(Boolean badwords) {
        this.badwords = badwords;
    }

    public List<Alb> getAlb() {
        return alb;
    }

    public void setAlb(List<Alb> alb) {
        this.alb = alb;
    }

}
