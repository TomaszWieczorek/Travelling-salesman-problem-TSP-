package com.company;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        //read from file
        String file_name = "berlin52";
        File file = readFile(file_name + ".txt");
        int subject_size = readSizeOfSubjectsFromFile(file);
        int[][] matrix_distance = readMatrixFromFile(file, subject_size);

        //creating population
        int population_size = 40;
        int[][] population = createPopulation(population_size, subject_size);

        //start timer
        long start_time = System.currentTimeMillis();

        //best subject variables
        int best_subject_length = Integer.MAX_VALUE;
        int[] best_subject = new int[subject_size];


        //main program
        for (int cycle_count = 0; cycle_count < 100000; cycle_count++) {
            int[][] population_after_selection = make_selection('T', population, matrix_distance);
            int[][] population_after_crossing = make_crossing(50, population_after_selection);
            int[][] population_after_mutation = make_mutation(70, population_after_crossing);

            for(int i = 0; i< population_after_mutation.length;i++)
            {
                for (int j = 0; j< population_after_mutation[i].length;j++)
                    population[i][j] = population_after_mutation[i][j];
            }

            //search for best subject
            if (returnBestSubjectLength(population, matrix_distance) < best_subject_length) {
                best_subject_length = returnBestSubjectLength(population, matrix_distance);
                int best_subject_index = returnBestSubjectIndex(population, matrix_distance);
                best_subject = copyOneDimensionArray(population[best_subject_index]);
                System.out.println(cycle_count + ". " + best_subject_length);
            }

        }

        //stop time
        long stop_time = System.currentTimeMillis();

        //summary and save to file
        //printSubjectWithLength(best_subject,matrix_distance);
        printTime(start_time,stop_time);
        printOneToFile(best_subject, matrix_distance, file_name + "_wynik" + ".txt");
        }


    //return methods

    private static File readFile(String file_name) {
        File temp_file = new File(file_name);
        if (!temp_file.exists()) {
            System.out.println("There is no file with name: '" + temp_file.getName() + "'.");
            System.exit(0);
        }
        System.out.println("Finding best solution from "+ file_name + ".");
        return temp_file;

    }

    private static int readSizeOfSubjectsFromFile(File file) {
        Scanner temp_scanner = null; //variable to scan file
        String temp_line; //variable to readLine
        int temp_subject_size = -1; //temp variable size of subjects
        try {
            System.out.println("Reading number from file...");
            temp_scanner = new Scanner(file);
            temp_line = temp_scanner.nextLine();
            temp_subject_size = Integer.parseInt(temp_line);
        } catch (IOException io) {
            System.out.println("file error");
        } finally {
            if (temp_scanner != null)
                temp_scanner.close();
        }
        System.out.println("Done.");
        return temp_subject_size;
    }

    private static int[][] readMatrixFromFile(File file, int subject_size) {
        int[][] temp_matrix_distance = null; //temporary variable for matrix of distance
        Scanner temp_scanner = null; //variable to scan file
        String temp_line; //variable to readLine
        String[] temp_line_array; //array variable to line.split
        try {
            System.out.println("Reading from file...");
            temp_scanner = new Scanner(file);
            temp_scanner.nextLine();
            temp_matrix_distance = new int[subject_size][subject_size];
            for (int i = 0; i < temp_matrix_distance.length; i++) {
                temp_line = temp_scanner.nextLine();
                temp_line_array = temp_line.split(" ");
                for (int j = 0; j < temp_line_array.length; j++) {
                    temp_matrix_distance[i][j] = Integer.parseInt(temp_line_array[j]);
                    temp_matrix_distance[j][i] = Integer.parseInt(temp_line_array[j]);
                }
            }
            System.out.println("Done.");
        } catch (IOException io) {
            System.out.println("file error");
        } finally {
            if (temp_scanner != null)
                temp_scanner.close();
        }
        return temp_matrix_distance;
    }

    private static int[][] createPopulation(int population_size, int subject_size) {
        //creating population
        int[][] temp_population = new int[population_size][subject_size];
        Random rnd = new Random();
        List<Integer> temp_gens = new ArrayList<Integer>();
        for (int i = 0; i < temp_population.length; i++) {
            for (int j = 0; j < temp_population[i].length; j++)
                temp_gens.add(j);
            for (int j = 0; j < temp_population[i].length; j++) {
                int temp_random_subject = rnd.nextInt(temp_gens.size());
                temp_population[i][j] = temp_gens.get(temp_random_subject);
                temp_gens.remove(temp_random_subject);
            }
        }
        return temp_population;
    }

    private static int[][] make_selection(char selection_type, int[][] population, int[][] matrix_distance) {
        int[][] temp_population_after_selection = new int[population.length][population[0].length];
        if (selection_type == 'T')
            temp_population_after_selection = tournament(4, population, matrix_distance);
        else if (selection_type == 'R')
            temp_population_after_selection = roulette(population, matrix_distance);
        else {
            System.out.println("Bad selection initial.");
            System.exit(0);
        }

        return temp_population_after_selection;
    }

    private static int[][] tournament(int torunament_group_size, int[][] population, int[][] matrix_distance) {
        int[][] temp_population_after_tournament = new int[population.length][population[0].length];
        int[] temp_best_index_after_tournament = new int[population.length];

        int[] tournament_subject_group;
        int minimum_subject_index, minimum_subject_length;

        for (int i = 0; i < population.length; i++) {
            tournament_subject_group = returnOneTournamentGroup(population.length, torunament_group_size);
            minimum_subject_index = -1;
            minimum_subject_length = Integer.MAX_VALUE;
            for (int j = 0; j < tournament_subject_group.length; j++) {
                if (returnOneSubjectLength(population[tournament_subject_group[j]], matrix_distance) < minimum_subject_length) {
                    minimum_subject_length = returnOneSubjectLength(population[tournament_subject_group[j]], matrix_distance);
                    minimum_subject_index = tournament_subject_group[j];
                }
            }
            temp_best_index_after_tournament[i] = minimum_subject_index;
        }

        for (int i = 0; i < temp_population_after_tournament.length; i++) {
            for (int j = 0; j < temp_population_after_tournament[i].length; j++) {
                temp_population_after_tournament[i][j] = population[temp_best_index_after_tournament[i]][j];
            }
        }
        return temp_population_after_tournament;
    }

    private static int[] returnOneTournamentGroup(int subject_size, int torunament_group_size) {
        int[] temp_group = new int[torunament_group_size];
        int temp_random_subject;
        Random rnd = new Random();
        List<Integer> temp_subjects = new ArrayList<Integer>();

        for (int j = 0; j < subject_size; j++)
            temp_subjects.add(j);
        for (int j = 0; j < torunament_group_size; j++) {
            temp_random_subject = rnd.nextInt(temp_subjects.size());
            temp_group[j] = temp_subjects.get(temp_random_subject);
            temp_subjects.remove(temp_random_subject);
        }
        return temp_group;
    }

    private static int[][] roulette(int[][] population, int[][] matrix_distance) {
        int[][] temp_population_after_roulette = new int[population.length][population[0].length];
        int[] temp_best_index_after_roulette = new int[population.length];
        int worst_subject_length = returnWorstSubjectLength(population, matrix_distance);
        int[] subjects_grade = new int[population.length];
        int sum_all_grades = 0, temp_sum, temp_index, random_number_grade;
        Random rnd = new Random();
        for (int i = 0; i < population.length; i++) {
            subjects_grade[i] = worst_subject_length + 1 - returnOneSubjectLength(population[i], matrix_distance);
            sum_all_grades += subjects_grade[i];
        }

        for (int i = 0; i < temp_best_index_after_roulette.length; i++) {
            random_number_grade = rnd.nextInt(sum_all_grades);
            temp_sum = 0;
            temp_index = 0;
            temp_sum += subjects_grade[temp_index];
            while (temp_sum <= random_number_grade) {
                temp_index++;
                temp_sum += subjects_grade[temp_index];
            }
            temp_best_index_after_roulette[i] = temp_index;
        }

        for (int i = 0; i < temp_population_after_roulette.length; i++) {
            for (int j = 0; j < temp_population_after_roulette[i].length; j++) {
                temp_population_after_roulette[i][j] = population[temp_best_index_after_roulette[i]][j];
            }
        }

        return temp_population_after_roulette;
    }

    private static int returnOneSubjectLength(int[] subject, int[][] matrix_distance) {
        int temp_subject_length = 0;
        for (int j = 0; j < subject.length - 1; j++) {
            temp_subject_length += matrix_distance[subject[j]][subject[j + 1]];
        }
        temp_subject_length += matrix_distance[subject[0]][subject[subject.length - 1]];
        return temp_subject_length;
    }

    private static int returnWorstSubjectLength(int[][] population, int[][] matrix_distance) {
        int temp_worst_subject_length = Integer.MIN_VALUE;
        for (int i = 0; i < population.length; i++) {
            if (returnOneSubjectLength(population[i], matrix_distance) > temp_worst_subject_length)
                temp_worst_subject_length = returnOneSubjectLength(population[i], matrix_distance);
        }
        return temp_worst_subject_length;
    }

    private static int[][] make_crossing(int crossing_probality, int[][] population_after_selection) {
        int[][] temp_population_after_crossing = new int[population_after_selection.length][population_after_selection[0].length];
        int start_random_PMX, stop_random_pmx, temp_city_pmx, temp_index_pmx;
        int[] child_1 = new int[population_after_selection[0].length], child_2 = new int[population_after_selection[0].length];
        Random rnd = new Random();

        for (int i = 0; i < temp_population_after_crossing.length; i++) {
            for (int j = 0; j < temp_population_after_crossing[i].length; j++) {
                temp_population_after_crossing[i][j] = population_after_selection[i][j];
            }
        }

        for (int i = 0; i < temp_population_after_crossing.length; i += 2) {
            if (rnd.nextInt(100) < crossing_probality) {
                start_random_PMX = rnd.nextInt(child_1.length);
                stop_random_pmx = rnd.nextInt(child_1.length);

                while (start_random_PMX == stop_random_pmx) {
                    stop_random_pmx = rnd.nextInt(child_1.length);
                }

                if (stop_random_pmx < start_random_PMX) {
                    int temp_stop = start_random_PMX;
                    start_random_PMX = stop_random_pmx;
                    stop_random_pmx = temp_stop;
                }

                //region fill childs with -1
                Arrays.fill(child_1, -1);
                Arrays.fill(child_2, -1);
                //endregion

                //region fill random range with original value of population
                for (int j = start_random_PMX; j <= stop_random_pmx; j++) {
                    child_1[j] = population_after_selection[i][j];
                    child_2[j] = population_after_selection[i + 1][j];
                }
                //endregion

                //region crossing child 1

                for (int j = 0; j < start_random_PMX; j++) {
                    temp_city_pmx = population_after_selection[i + 1][j];
                    temp_index_pmx = returnIndex(child_1, temp_city_pmx);
                    while (temp_index_pmx != -1) {
                        temp_city_pmx = population_after_selection[i + 1][temp_index_pmx];
                        temp_index_pmx = returnIndex(child_1, temp_city_pmx);
                    }
                    child_1[j] = temp_city_pmx;
                }

                for (int j = stop_random_pmx + 1; j < child_1.length; j++) {
                    temp_city_pmx = population_after_selection[i + 1][j];
                    temp_index_pmx = returnIndex(child_1, temp_city_pmx);
                    while (temp_index_pmx != -1) {
                        temp_city_pmx = population_after_selection[i + 1][temp_index_pmx];
                        temp_index_pmx = returnIndex(child_1, temp_city_pmx);
                    }
                    child_1[j] = temp_city_pmx;
                }

                //endregion

                //region crossing child 2

                for (int j = 0; j < start_random_PMX; j++) {
                    temp_city_pmx = population_after_selection[i][j];
                    temp_index_pmx = returnIndex(child_2, temp_city_pmx);
                    while (temp_index_pmx != -1) {
                        temp_city_pmx = population_after_selection[i][temp_index_pmx];
                        temp_index_pmx = returnIndex(child_2, temp_city_pmx);
                    }
                    child_2[j] = temp_city_pmx;
                }

                for (int j = stop_random_pmx + 1; j < child_1.length; j++) {
                    temp_city_pmx = population_after_selection[i][j];
                    temp_index_pmx = returnIndex(child_2, temp_city_pmx);
                    while (temp_index_pmx != -1) {
                        temp_city_pmx = population_after_selection[i][temp_index_pmx];
                        temp_index_pmx = returnIndex(child_2, temp_city_pmx);
                    }
                    child_2[j] = temp_city_pmx;
                }

                //endregion

                for (int j = 0; j < population_after_selection[i].length; j++) {
                    temp_population_after_crossing[i][j] = child_1[j];
                    temp_population_after_crossing[i + 1][j] = child_2[j];
                }

            }
        }
        return temp_population_after_crossing;
    }

    private static int returnIndex(int[] subject, int gene) {
        for (int i = 0; i < subject.length; i++)
            if (subject[i] == gene) return i;
        return -1;
    }

    private static int[][] make_mutation(int mutation_probality, int[][] population_after_crossing) {
        int[][] temp_population_after_mutation = new int[population_after_crossing.length][population_after_crossing[0].length];
        int start_random_mutation, stop_random_mutation;
        Random rnd = new Random();
        for (int i = 0; i < temp_population_after_mutation.length; i++) {
            for (int j = 0; j < temp_population_after_mutation[i].length; j++) {
                temp_population_after_mutation[i][j] = population_after_crossing[i][j];
            }

        }
        for (int i = 0; i < population_after_crossing.length; i++) {
            if( rnd.nextInt(100) < mutation_probality) {
                start_random_mutation = rnd.nextInt(population_after_crossing[i].length);
                stop_random_mutation = rnd.nextInt(population_after_crossing[i].length);

                while (start_random_mutation == stop_random_mutation) {
                    stop_random_mutation = rnd.nextInt(population_after_crossing[i].length);
                }

                if (stop_random_mutation < start_random_mutation) {
                    int temp_stop = start_random_mutation;
                    start_random_mutation = stop_random_mutation;
                    stop_random_mutation = temp_stop;
                }

                int[] temp_array = new int[stop_random_mutation - start_random_mutation];
                for (int j = 0; j < temp_array.length; j++) {
                    temp_array[j] = temp_population_after_mutation[i][start_random_mutation + j];
                }
                temp_array = reverseArray(temp_array);
                for (int j = 0; j < temp_array.length; j++) {
                    temp_population_after_mutation[i][start_random_mutation + j] = temp_array[j];
                }
            }
        }
        return temp_population_after_mutation;
    }

    private static int[] reverseArray(int[] array) {
        int[] temp_reverse_array = new int[array.length];
        for (int i = 0; i < array.length / 2; i++) {
            int temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
        for (int i = 0; i < array.length; i++) {
            temp_reverse_array[i] = array[i];
        }
        return temp_reverse_array;
    }

    private static int returnBestSubjectLength(int[][] population, int[][] matrix_distance) {
        int temp_best_subject_length = Integer.MAX_VALUE;
        for (int i = 0; i < population.length; i++) {
            if (returnOneSubjectLength(population[i], matrix_distance) < temp_best_subject_length)
                temp_best_subject_length = returnOneSubjectLength(population[i], matrix_distance);
        }
        return temp_best_subject_length;
    }

    private static int[] copyOneDimensionArray(int[] array) {
        int[] temp_final_array = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            temp_final_array[i] = array[i];
        }
        return temp_final_array;
    }

    private static int returnBestSubjectIndex(int[][] population, int[][] matrix_distance) {
        int temp_best_subject_length = Integer.MAX_VALUE;
        int temp_best_subject_index = -1;
        for (int i = 0; i < population.length; i++) {
            if (returnOneSubjectLength(population[i], matrix_distance) < temp_best_subject_length) {
                temp_best_subject_length = returnOneSubjectLength(population[i], matrix_distance);
                temp_best_subject_index = i;
            }
        }
        return temp_best_subject_index;
    }



    //print methods
    private static void printSubjectWithLength(int[] best_subject, int[][] matrix_distance) {
        for (int i = 0; i< best_subject.length;i++)
        {
            if (i == (best_subject.length - 1))
                System.out.printf("%d", best_subject[i]);
            else
                System.out.printf("%d-", best_subject[i]);

        }
        System.out.println(" " + returnOneSubjectLength(best_subject, matrix_distance));

    }

    private static void printTime(long start_time, long stop_time) {
        long time_minute = (stop_time - start_time) / 1000 / 60;
        long time_second = ((stop_time - start_time) / 1000) % 60;
        long time_milisecond = ((stop_time - start_time) % 1000) % 60;
        long time = (stop_time - start_time);
        System.out.printf("Time: %d m %d s %d ms (%d ms).", time_minute, time_second, time_milisecond, time);
        System.out.println();
    }

    private static void printOneToFile(int[] subject, int[][] matrix_distance, String file_name) {
        PrintWriter tempFile = null;
        try {
            tempFile = new PrintWriter(file_name);
            for (int i = 0; i < subject.length; i++) {
                if (i == (subject.length - 1))
                    tempFile.printf("%d", subject[i]);
                else
                    tempFile.printf("%d-", subject[i]);
            }
            tempFile.print(" " + returnOneSubjectLength(subject, matrix_distance));
        } catch (IOException io) {
            System.out.println("blad");
        } finally {
            if (tempFile != null)
                tempFile.close();
            System.out.println("Successfully printed best road to file.");
        }
    }



}
